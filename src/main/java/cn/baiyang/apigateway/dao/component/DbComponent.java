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

	private static Date lastModifiedTime; // 本次路由数据检查，最新一条路由更新的时间

	/** 路由修改记录 */
	private Map<String, Long> modifyMap = new HashMap<>();

	/** 版本map */
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
				logger.error("执行定时任务时发生异常: ex= {}", e.getMessage());
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
	 * 加载需要重定向的链接配置
	 */
	private void loadAllRedirectUrl() {
		List<RedirectConfigDO> redirectConfigs = redirectConfigMapper.selectAll();
		if (CollectionUtils.isEmpty(redirectConfigs)) {
			return;
		}

		redirectConfigs.forEach(redirectConfigDO -> {
			// 此条记录不生效
			if (Objects.equals(redirectConfigDO.getIsValid(),
					RedirectIsValidEnum.NO.getIsValid())) {
				redirectUrlMap.remove(redirectConfigDO.getSourceUrl());
				return;
			}
			RedirectConfigDO targetUrl = redirectUrlMap
					.get(redirectConfigDO.getSourceUrl());
			// 此条记录是否新增或有更新
			boolean isModify = Objects.isNull(targetUrl)
					|| !Objects.equals(targetUrl.getGmtModified(),
							redirectConfigDO.getGmtModified());

			if (!isModify) {
				return;
			}
			redirectUrlMap.put(redirectConfigDO.getSourceUrl(), redirectConfigDO);
		});

	}

	/** 根据原Url获取重定向目标url */
	public String getRedirectUrl(String sourceUrl) {
		if (StringUtils.isBlank(sourceUrl)) {

			return null;
		}
		RedirectConfigDO redirectConfigDO = redirectUrlMap.get(sourceUrl);
		return Objects.isNull(redirectConfigDO) ? null : redirectConfigDO.getTargetUrl();
	}

	/**
	 * 检查版本签名配置修改
	 */
	private void checkVersionKeyConfigModify() {
		List<VersionKeyConfigDO> configDOList = versionKeyConfigMapper
				.listVersionKeyConfig();
		if (CollectionUtils.isEmpty(configDOList)) {
			logger.warn("未获取到版本签名配置信息，查询结果为空");
			return;
		}
		// 根据appId分组
		Map<String, List<VersionKeyConfigDO>> groupMap = configDOList.stream()
				.collect(Collectors.groupingBy(VersionKeyConfigDO::getAppId));
		groupMap.forEach((appId, list) -> {
			List<BigDecimal> versionNameList = list.stream()
					.map(versionKeyConfigDO -> new BigDecimal(
							versionKeyConfigDO.getVersionName()))
					.sorted().collect(Collectors.toList());
			// 不存在时，新增
			if (!versionKeyConfigMap.containsKey(appId)) {
				versionKeyConfigMap.put(appId, versionNameList);
			}
			else {
				List<BigDecimal> oldList = versionKeyConfigMap.get(appId);
				// 检查是否包含所有的，若不包含就进行替换
				if (null != oldList && !oldList.containsAll(versionNameList)) {
					logger.info(
							"版本签名发生变更，appId = {},oldVersionList = {},newVersionList = {}",
							appId, JSONUtils.toJSONString(oldList),
							JSONUtils.toJSONString(versionNameList));
					versionKeyConfigMap.put(appId, versionNameList);
				}
			}
		});
	}

	/**
	 * 检查修改文件
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
				// 如果已经删除，将路由配置从内存中清除
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
			if (list.size() < pageSize) { // 已达最后一页，退出while循环
				break;
			}
		}
		logger.info("当前生效的路由配置数: {}, 耗费时间: {}", routeEntities.size(),
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
						logger.error("生成privateKey出错,appid={},error={}",
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
			throw new RouteException("", "路由表为空");
		}
		return routeEntities;
	}

	/**
	 * 根据用户角色获得版本信息
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
		// 先准确版本获取，如果不能获取到，尝试从旧版本配置获取
		if (privateKeyMap.containsKey(appId + version)) {
			return privateKeyMap.get(appId + version);
		}
		List<BigDecimal> versionList = versionKeyConfigMap.get(appId);
		// 没有配置，取默认
		if (CollectionUtils.isEmpty(versionList)) {
			return privateKeyMap.get(appId);
		}
		BigDecimal versionNo = new BigDecimal(version);
		int size = versionList.size();
		BigDecimal maxVersion = versionList.get(size - 1);
		// 配置的比最大一个都大，取最大一个
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
		// 没有更小的版本号，从version_client表取
		if (index < 0) {
			return privateKeyMap.get(appId);
		}
		// 从更小的版本号中取
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
			logger.error("生成privateKey出错,appid={},error={}", versionDO.getAppId(), e);
		}
	}

	/** 根据用户角色获得版本信息 */
	public VersionInfoDO getVersionInfoByClientIdAndVersion(Long clientId,
			String version) {
		if (Objects.isNull(clientId) || StringUtils.isBlank(version)) {
			return null;
		}
		String key = generateVersionClientKey(clientId, version);
		return clientVersionInfoMap.get(key);
	}

}
