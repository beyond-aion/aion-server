package com.aionemu.loginserver.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * This class provides useful methods to use with accounts.
 * 
 * @author SoulKeeper
 */
public class AccountUtils {

	/**
	 * Encodes password. SHA-1 is used to encode password bytes, Base64 wraps SHA1-hash to string.
	 * 
	 * @param password
	 *          password to encode
	 * @return the encoded password
	 */
	public static String encodePassword(String password) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(password.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new Error("Exception while encoding password", e);
		}
	}
}
