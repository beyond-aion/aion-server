package com.aionemu.gameserver.services.player;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SECURITY_TOKEN_REQUEST_STATUS;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_CHANGE_SESSION_ID;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Artur
 */
public class SecurityTokenService {

	private final Logger log = LoggerFactory.getLogger(SecurityTokenService.class);

	public void generateToken(Player player) {
		String token = MD5(player.getName() + System.currentTimeMillis());
		player.getPlayerAccount().setSecurityToken(token);
		sendToken(player, player.getPlayerAccount().getSecurityToken());
		LoginServer.getInstance().sendPacket(
			new SM_CHANGE_SESSION_ID(player.getPlayerAccount().getId(), player.getPlayerAccount().getSecurityToken().substring(0, 16)));
	}

	public void sendToken(Player player, String token) {
		PacketSendUtility.sendPacket(player, new SM_SECURITY_TOKEN_REQUEST_STATUS(token));
	}

	private String MD5(String md5) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			log.warn("[SecurityToken] Error to generate token for player!");
		}
		return null;
	}

	public static final SecurityTokenService getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final SecurityTokenService instance = new SecurityTokenService();
	}
}
