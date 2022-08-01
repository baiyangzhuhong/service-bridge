package cn.baiyang.apigateway.util;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public class RSAUtils {

	private static String encoding = "utf-8";

	// RSA最大加密明文大小
	private static final int MAX_ENCRYPT_BLOCK = 117;

	// RSA最大解密密文大小
	private static final int MAX_DECRYPT_BLOCK = 128;

	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	private static final String ENDE_ALGORITHMS = "RSA/ECB/PKCS1Padding";

	// 获得公钥Key
	public static PublicKey getPublicKey(String publicKeyStr) throws Exception {
		byte[] decodedKey = Base64.decodeBase64(publicKeyStr.getBytes(encoding));
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(decodedKey);
		PublicKey pubkey = keyFactory.generatePublic(x509KeySpec);
		return pubkey;
	}

	// 获得私钥Key
	public static PrivateKey getPrivateKey(String privateKeyStr) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(privateKeyStr.getBytes(encoding));
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	// 用私钥对信息生成数字签名
	public static String sign(String info, PrivateKey privateKey) throws Exception {
		Signature signet = Signature.getInstance(SIGN_ALGORITHMS);
		signet.initSign(privateKey);
		signet.update(info.getBytes(encoding));
		byte[] signed = signet.sign(); // 对信息的数字签名
		byte[] base64Byte = Base64.encodeBase64(signed);
		return new String(base64Byte, encoding);
	}

	// 公钥校验数字签名
	public static boolean verify(String info, String signed, PublicKey pubkey)
			throws Exception {
		Signature signetcheck = Signature.getInstance(SIGN_ALGORITHMS);
		signetcheck.initVerify(pubkey);
		byte[] infoByte = info.getBytes(encoding);
		byte[] base64Byte = Base64.decodeBase64(signed.getBytes(encoding));
		signetcheck.update(infoByte);
		return signetcheck.verify(base64Byte);
	}

	// 加密
	public static String encrypt(String originData, Key key) throws Exception {
		byte[] dataBytes = originData.getBytes(encoding);
		int inputLen = dataBytes.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		Cipher cipher = Cipher.getInstance(ENDE_ALGORITHMS);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(dataBytes, offSet, MAX_ENCRYPT_BLOCK);
			}
			else {
				cache = cipher.doFinal(dataBytes, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		encryptedData = Base64.encodeBase64(encryptedData);
		return new String(encryptedData, encoding);
	}

	// 解密
	public static String decrypt(String encryptData, Key key) throws Exception {
		byte[] encryptByte = encryptData.getBytes(encoding);
		encryptByte = Base64.decodeBase64(encryptByte);
		int inputLen = encryptByte.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		Cipher cipher = Cipher.getInstance(ENDE_ALGORITHMS);
		cipher.init(Cipher.DECRYPT_MODE, key);
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptByte, offSet, MAX_DECRYPT_BLOCK);
			}
			else {
				cache = cipher.doFinal(encryptByte, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return new String(decryptedData, encoding);
	}

}
