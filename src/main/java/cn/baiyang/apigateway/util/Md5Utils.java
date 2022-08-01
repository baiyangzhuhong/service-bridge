package cn.baiyang.apigateway.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

	/**
	 * 编码格式
	 */
	private static String encoding = "utf-8";

	/**
	 * MD5加密
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String getMD5(String data) throws Exception {
		byte[] bytes = data.getBytes(encoding);
		return getMD5(bytes);
	}

	/**
	 * MD5加密
	 * @param src
	 * @return
	 * @throws java.security.NoSuchAlgorithmException
	 */
	public static String getMD5(byte[] src) throws NoSuchAlgorithmException {
		StringBuffer sb = new StringBuffer();

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(src);
		for (byte b : md.digest()) {
			sb.append(Integer.toString(b >>> 4 & 0xF, 16))
					.append(Integer.toString(b & 0xF, 16));
		}

		return sb.toString();
	}

}
