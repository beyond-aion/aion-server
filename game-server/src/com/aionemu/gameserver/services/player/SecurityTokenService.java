package com.aionemu.gameserver.services.player;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SECURITY_TOKEN_REQUEST_STATUS;

/**
 * @author Artur
 */
public class SecurityTokenService {

	private final Logger log = LoggerFactory.getLogger(SecurityTokenService.class);

	public void generateToken(Account account, AionConnection con) {
		String token = MD5(account.getName() + System.currentTimeMillis());
		account.setSecurityToken(token);
		sendToken(con, account.getSecurityToken());
		//LoginServer.getInstance().sendPacket(new SM_CHANGE_SESSION_ID(account.getId(), account.getSecurityToken().substring(0, 16)));
	}

	public void sendToken(AionConnection con, String token) {
		con.sendPacket(new SM_SECURITY_TOKEN_REQUEST_STATUS(token));
	}

	private String MD5(String md5) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte element : array) {
				sb.append(Integer.toHexString((element & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			log.warn("[SecurityToken] Error to generate token for player!");
		}
		return null;
	}

	public static SecurityTokenService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final SecurityTokenService instance = new SecurityTokenService();
	}
}
