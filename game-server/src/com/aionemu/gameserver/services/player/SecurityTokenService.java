package com.aionemu.gameserver.services.player;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.account.Account;

/**
 * @author Artur
 */
public class SecurityTokenService {

	private SecurityTokenService() {
	}

	public static void generateToken(Account account) {
		String token = MD5(account.getName() + System.currentTimeMillis());
		account.setSecurityToken(token);
	}

	private static String MD5(String md5) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte element : array) {
				sb.append(Integer.toHexString((element & 0xFF) | 0x100), 1, 3);
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			LoggerFactory.getLogger(SecurityTokenService.class).warn("Could not generate token", e);
		}
		return null;
	}
}
