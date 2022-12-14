package cn.baiyang.apigateway.dao.component;

import com.alibaba.druid.support.json.JSONUtils;
import cn.baiyang.apigateway.common.DbInitEvent;
import cn.baiyang.apigateway.common.PageQuery;
import cn.baiyang.apigateway.config.ApiGatewayProperties;
import cn.baiyang.apigateway.dao.component.vo.RouteEntity;
import cn.baiyang.apigateway.dao.component.vo.VersionEntity;
import cn.baiyang.apigateway.dao.redirectconfig.RedirectConfigMapper;
import cn.baiyang.apigateway.dao.redirectconfig.model.RedirectConfigDO;
import cn.baiyang.apigateway.dao.routeconfig.RouteConfigMapper;
import cn.baiyang.apigateway.dao.routeconfig.model.RouteConfigDO;
import cn.baiyang.apigateway.dao.version.VersionInfoMapper;
import cn.baiyang.apigateway.dao.version.VersionKeyConfigMapper;
import cn.baiyang.apigateway.dao.version.VersionMapper;
import cn.baiyang.apigateway.dao.version.model.SignKeyDO;
import cn.baiyang.apigateway.dao.version.model.VersionDO;
import cn.baiyang.apigateway.dao.version.model.VersionInfoDO;
import cn.baiyang.apigateway.dao.version.model.VersionKeyConfigDO;
import cn.baiyang.apigateway.enums.RedirectIsValidEnum;
import cn.baiyang.apigateway.exception.RouteException;
import cn.baiyang.apigateway.init.discovery.RouteEntityObservable;
import cn.baiyang.apigateway.util.RSAUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class DbComponent {

	public static final Logger logger = LoggerFactory.getLogger(DbComponent.class);

	@Value("${plugins.enabled}")
	private Boolean pluginsEnabled;

	@Resource
	private ApiGatewayProperties apiGatewayProperties;

	@Autowired
	private RouteEntityObservable routeEntityObservable;

	@Resource
	private VersionMapper versionMapper;

	@Resource
	private RouteConfigMapper routeConfigMapper;

	@Resource
	private VersionKeyConfigMapper versionKeyConfigMapper;

	@Resource
	private RedirectConfigMapper redirectConfigMapper;

	@Resource
	private VersionInfoMapper versionInfoMapper;

	@Autowired
	private ApplicationContext applicationContext;

	private static Date lastModifiedTime; // ????????????????????????????????????????????????????????????

	/** ?????????????????? */
	private Map<String, Long> modifyMap = new HashMap<>();

	/** ??????map */
	private Map<String, VersionEntity> versionMap = new ConcurrentHashMap<>();

	private Map<String, PrivateKey> privateKeyMap = new ConcurrentHashMap<>();

	private ConcurrentMap<String, List<BigDecimal>> versionKeyConfigMap = new ConcurrentHashMap<>();

	private Set<RouteEntity> routeEntities = new CopyOnWriteArraySet<>();

	private Map<String, RedirectConfigDO> redirectUrlMap = new ConcurrentHashMap<>();

	private Map<String, VersionInfoDO> clientVersionInfoMap = new ConcurrentHashMap<>();

	@EventListener
	public void init(ApplicationReadyEvent event) {
		LocalDate localDate = LocalDate.of(1970, 1, 1);
		lastModifiedTime = Date
				.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

		Runnable runnable = () -> {
			try {
				checkRouteModify();
				if (null != pluginsEnabled && pluginsEnabled) {
					loadAllRedirectUrl();
					checkVersionConfigModify();
					checkVersionKeyConfigModify();
					checkClientVersionInfoModify();
					checkSignKeyModify();
				}
			}
			catch (Exception e) {
				logger.error("?????????????????????????????????: ex= {}", e.getMessage());
			}
		};
		runnable.run();
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
		executorService.scheduleAtFixedRate(runnable,
				apiGatewayProperties.getVersionConfigTime(),
				apiGatewayProperties.getVersionConfigTime(), TimeUnit.MILLISECONDS);
		applicationContext.publishEvent(new DbInitEvent(this));
	}

	/**
	 * ????????????????????????????????????
	 */
	private void loadAllRedirectUrl() {
		List<RedirectConfigDO> redirectConfigs = redirectConfigMapper.selectAll();
		if (CollectionUtils.isEmpty(redirectConfigs)) {
			return;
		}

		redirectConfigs.forEach(redirectConfigDO -> {
			// ?????????????????????
			if (Objects.equals(redirectConfigDO.getIsValid(),
					RedirectIsValidEnum.NO.getIsValid())) {
				redirectUrlMap.remove(redirectConfigDO.getSourceUrl());
				return;
			}
			RedirectConfigDO targetUrl = redirectUrlMap
					.get(redirectConfigDO.getSourceUrl());
			// ????????????????????????????????????
			boolean isModify = Objects.isNull(targetUrl)
					|| !Objects.equals(targetUrl.getGmtModified(),
							redirectConfigDO.getGmtModified());

			if (!isModify) {
				return;
			}
			redirectUrlMap.put(redirectConfigDO.getSourceUrl(), redirectConfigDO);
		});

	}

	/** ?????????Url?????????????????????url */
	public String getRedirectUrl(String sourceUrl) {
		if (StringUtils.isBlank(sourceUrl)) {

			return null;
		}
		RedirectConfigDO redirectConfigDO = redirectUrlMap.get(sourceUrl);
		return Objects.isNull(redirectConfigDO) ? null : redirectConfigDO.getTargetUrl();
	}

	/**
	 * ??????????????????????????????
	 */
	private void checkVersionKeyConfigModify() {
		List<VersionKeyConfigDO> configDOList = versionKeyConfigMapper
				.listVersionKeyConfig();
		if (CollectionUtils.isEmpty(configDOList)) {
			logger.warn("?????????????????????????????????????????????????????????");
			return;
		}
		// ??????appId??????
		Map<String, List<VersionKeyConfigDO>> groupMap = configDOList.stream()
				.collect(Collectors.groupingBy(VersionKeyConfigDO::getAppId));
		groupMap.forEach((appId, list) -> {
			List<BigDecimal> versionNameList = list.stream()
					.map(versionKeyConfigDO -> new BigDecimal(
							versionKeyConfigDO.getVersionName()))
					.sorted().collect(Collectors.toList());
			// ?????????????????????
			if (!versionKeyConfigMap.containsKey(appId)) {
				versionKeyConfigMap.put(appId, versionNameList);
			}
			else {
				List<BigDecimal> oldList = versionKeyConfigMap.get(appId);
				// ?????????????????????????????????????????????????????????
				if (null != oldList && !oldList.containsAll(versionNameList)) {
					logger.info(
							"???????????????????????????appId = {},oldVersionList = {},newVersionList = {}",
							appId, JSONUtils.toJSONString(oldList),
							JSONUtils.toJSONString(versionNameList));
					versionKeyConfigMap.put(appId, versionNameList);
				}
			}
		});
	}

	/**
	 * ??????????????????
	 */
	private void checkVersionConfigModify() {
		PageQuery pageQuery = new PageQuery();
		pageQuery.setPageSize(PageQuery.MAX_PAGE_SIZE);
		List<VersionDO> list = versionMapper.listVersionInfo(pageQuery);
		while (CollectionUtils.isNotEmpty(list)) {
			for (VersionDO versionDO : list) {

				logger.debug("checkVersionConfigModifyFile,appId={},gmtModified={}",
						versionDO.getAppId(), versionDO.getGmtModified().getTime());

				loadVersionConfigFile(versionDO);
			}
			pageQuery.setPageNumber(pageQuery.getPageNumber() + 1);
			list = versionMapper.listVersionInfo(pageQuery);
		}
		logger.info("Current version config records size:{}", versionMap.size());
	}

	private void checkClientVersionInfoModify() {
		PageQuery pageQuery = new PageQuery();
		pageQuery.setPageSize(PageQuery.MAX_PAGE_SIZE);
		List<VersionInfoDO> versionInfoDOS = versionInfoMapper.listVersionInfo(pageQuery);
		while (CollectionUtils.isNotEmpty(versionInfoDOS)) {
			versionInfoDOS.forEach(versionInfoDO -> loadClientVersionInfo(versionInfoDO));
			pageQuery.setPageNumber(pageQuery.getPageNumber() + 1);
			versionInfoDOS = versionInfoMapper.listVersionInfo(pageQuery);
		}
	}

	private void loadClientVersionInfo(VersionInfoDO versionInfoDO) {
		if (Objects.isNull(versionInfoDO)) {
			return;
		}
		String versionClientKey = generateVersionClientKey(
				versionInfoDO.getVersionClientId(), versionInfoDO.getVersionNumber());

		clientVersionInfoMap.put(versionClientKey, versionInfoDO);
	}

	private String generateVersionClientKey(Long versionClientId, String versionNumber) {
		return versionClientId + ":" + versionNumber;
	}

	private void checkRouteModify() {
		int offset = 0;
		int pageSize = 100;
		Date tempLastModifiedTime = new Date(lastModifiedTime.getTime());
		long startTime = System.currentTimeMillis();
		while (true) {
			List<RouteConfigDO> list = routeConfigMapper.listByPage(tempLastModifiedTime,
					offset, pageSize);
			if (CollectionUtils.isEmpty(list)) {
				break;
			}
			offset += list.size();
			for (RouteConfigDO routeConfigDO : list) {
				String routeUniqueKey = "RouteConfig:" + routeConfigDO.getIdc()
						+ routeConfigDO.getPath();
				Long oldModifyTime = modifyMap.get(routeUniqueKey);
				RouteEntity entity = new RouteEntity();
				entity.setIdc(routeConfigDO.getIdc());
				entity.setEnableEureka(routeConfigDO.getEnableEureka() == 1);
				entity.setStripPrefix(routeConfigDO.getStripPrefix());
				entity.setPath(routeConfigDO.getPath());
				entity.setServer(routeConfigDO.getServer());
				entity.setServiceId(routeConfigDO.getServiceId());
				if (lastModifiedTime.getTime() < routeConfigDO.getGmtModified()
						.getTime()) {
					lastModifiedTime.setTime(routeConfigDO.getGmtModified().getTime());
				}
				// ??????????????????????????????????????????????????????
				if (Objects.equals(routeConfigDO.getIsDeleted(), 1)) {
					routeEntities.remove(entity);
					modifyMap.remove(routeUniqueKey);
					// routeEntityObservable.routeChanged(routeConfigDO);
					continue;
				}
				if (oldModifyTime == null) {
					routeEntityObservable.routeChanged(routeConfigDO);
				}
				if (oldModifyTime == null
						|| routeConfigDO.getGmtModified().getTime() != oldModifyTime) {
					routeEntities.remove(entity);
					routeEntities.add(entity);
					modifyMap.put(routeUniqueKey,
							routeConfigDO.getGmtModified().getTime());
				}
			}
			if (list.size() < pageSize) { // ???????????????????????????while??????
				break;
			}
		}
		logger.info("??????????????????????????????: {}, ????????????: {}", routeEntities.size(),
			(System.currentTimeMillis() - startTime));
	}

	private void checkSignKeyModify() {
		List<SignKeyDO> list = versionMapper.listSignKeys();
		if (CollectionUtils.isNotEmpty(list)) {
			for (SignKeyDO signKeyDO : list) {
				String fileName = "Signkey:" + signKeyDO.getAppId()
						+ signKeyDO.getVersion();
				Long fileModify = modifyMap.get(fileName);

				if (fileModify == null
						|| signKeyDO.getGmtModified().getTime() != fileModify) {
					try {
						privateKeyMap.put(signKeyDO.getAppId() + signKeyDO.getVersion(),
								RSAUtils.getPrivateKey(signKeyDO.getRsaPrivateKey()));
					}
					catch (Exception e) {
						logger.error("??????privateKey??????,appid={},error={}",
								signKeyDO.getAppId(), e);
					}

					modifyMap.put(fileName, signKeyDO.getGmtModified().getTime());
				}
			}
		}
	}

	/**
	 * @exception cn.baiyang.apigateway.exception.RouteException
	 */
	public Set<RouteEntity> getRouteEntities() {
		Set<RouteEntity> routeEntities = Collections.unmodifiableSet(this.routeEntities);
		if (CollectionUtils.isEmpty(routeEntities)) {
			throw new RouteException("", "???????????????");
		}
		return routeEntities;
	}

	/**
	 * ????????????????????????????????????
	 */
	public VersionEntity getVersionByAppId(String appId) {
		if (StringUtils.isBlank(appId)) {
			return null;
		}
		return versionMap.get(appId);
	}

	public PrivateKey getPrivateKey(String appId, String version) {
		if (StringUtils.isBlank(appId)) {
			return null;
		}
		// ??????????????????????????????????????????????????????????????????????????????
		if (privateKeyMap.containsKey(appId + version)) {
			return privateKeyMap.get(appId + version);
		}
		List<BigDecimal> versionList = versionKeyConfigMap.get(appId);
		// ????????????????????????
		if (CollectionUtils.isEmpty(versionList)) {
			return privateKeyMap.get(appId);
		}
		BigDecimal versionNo = new BigDecimal(version);
		int size = versionList.size();
		BigDecimal maxVersion = versionList.get(size - 1);
		// ????????????????????????????????????????????????
		if (maxVersion.compareTo(versionNo) < 0) {
			return privateKeyMap.get(appId + versionList.get(size - 1));
		}
		int index = -1;
		for (int i = 0; i < size; i++) {
			if (versionList.get(i).compareTo(versionNo) > 0) {
				index = i - 1;
				break;
			}
		}
		// ??????????????????????????????version_client??????
		if (index < 0) {
			return privateKeyMap.get(appId);
		}
		// ???????????????????????????
		return privateKeyMap.get(appId + versionList.get(index));
	}

	private void loadVersionConfigFile(VersionDO versionDO) {
		VersionEntity entity = new VersionEntity();
		entity.setVersionClientId(versionDO.getVersionClientId());
		entity.setAppId(versionDO.getAppId());
		entity.setLowestVersion(Float.parseFloat(versionDO.getLowestVersion()));
		entity.setSourceId(versionDO.getSourceId());
		entity.setSmsSignature(versionDO.getSmsSignature());
		entity.setSignVersion(versionDO.getSignVersion());
		versionMap.put(entity.getAppId(), entity);

		try {
			String privateKeyStr = versionDO.getRasPrivateKey();
			if (StringUtils.isNotBlank(privateKeyStr)) {
				privateKeyMap.put(versionDO.getAppId(),
						RSAUtils.getPrivateKey(privateKeyStr));
			}
		}
		catch (Exception e) {
			logger.error("??????privateKey??????,appid={},error={}", versionDO.getAppId(), e);
		}
	}

	/** ???????????????????????????????????? */
	public VersionInfoDO getVersionInfoByClientIdAndVersion(Long clientId,
			String version) {
		if (Objects.isNull(clientId) || StringUtils.isBlank(version)) {
			return null;
		}
		String key = generateVersionClientKey(clientId, version);
		return clientVersionInfoMap.get(key);
	}

}
