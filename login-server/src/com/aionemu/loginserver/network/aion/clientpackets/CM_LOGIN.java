package com.aionemu.loginserver.network.aion.clientpackets;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.Iterator;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.controller.BannedIpController;
import com.aionemu.loginserver.network.aion.AionAuthResponse;
import com.aionemu.loginserver.network.aion.AionClientPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.aion.LoginConnection.State;
import com.aionemu.loginserver.network.aion.SessionKey;
import com.aionemu.loginserver.network.aion.serverpackets.SM_LOGIN_FAIL;
import com.aionemu.loginserver.network.aion.serverpackets.SM_LOGIN_OK;
import com.aionemu.loginserver.utils.BruteForceProtector;
import com.google.common.primitives.Bytes;
import com.mysql.jdbc.StringUtils;

/**
 * @author -Nemesiss-, KID, Lyahim, Rolandas
 */
public class CM_LOGIN extends AionClientPacket {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(CM_LOGIN.class);

	/**
	 * byte array contains encrypted login and password.
	 */
	private byte[] data1;
	private byte[] data2;
	@SuppressWarnings("unused")
	private int sessionId;

	/**
	 * Constructs new instance of <tt>CM_LOGIN </tt> packet.
	 * 
	 * @param buf
	 * @param client
	 */
	public CM_LOGIN(ByteBuffer buf, LoginConnection client) {
		super(buf, client, 0x00);
	}

	@Override
	protected void readImpl() {
		data1 = readB(128);
		data2 = readB(128);
		sessionId = readD();
		readB(16); // zeroes
		readB(7); // 0x20
		readB(16); // static bytes: 34 3A 0E F7 AE F3 9A E8 2D 09 7F 0B E0 19 78 89
		readD(); // 0x0
		readD(); // unk
		readD(); // 0x0
	}

	@Override
	protected void runImpl() {
		byte[] decrypted1, decrypted2;
		try {
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, getConnection().getRSAPrivateKey());
			decrypted1 = rsaCipher.doFinal(data1, 0, 128);
		} catch (GeneralSecurityException e) {
			sendPacket(new SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR));
			return;
		}

		try {
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, getConnection().getRSAPrivateKey());
			decrypted2 = rsaCipher.doFinal(data2, 0, 128);
		} catch (GeneralSecurityException e) {
			sendPacket(new SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR));
			return;
		}

		Iterator<Byte> bytes = Bytes.asList(decrypted1).iterator();
		String user = readStringFromBlock(bytes);
		String password = readStringFromBlock(bytes);

		if (StringUtils.isNullOrEmpty(password)) {
			// Second block if -loginex switch in client was used.
			bytes = Bytes.asList(decrypted2).iterator();
			password = readStringFromBlock(bytes);
			if (StringUtils.isNullOrEmpty(password)) {
				sendPacket(new SM_LOGIN_FAIL(AionAuthResponse.INVALID_PASSWORD3));
				return;
			}
		}

		LoginConnection client = getConnection();
		AionAuthResponse response = AccountController.login(user, password, client);
		switch (response) {
			case AUTHED:
				client.setState(State.AUTHED_LOGIN);
				client.setSessionKey(new SessionKey(client.getAccount()));
				client.sendPacket(new SM_LOGIN_OK(client.getSessionKey()));
				log.debug("" + user + " got authed state");
				break;
			case INVALID_PASSWORD:
			case INVALID_PASSWORD2:
			case INVALID_PASSWORD3:
				if (Config.ENABLE_BRUTEFORCE_PROTECTION) {
					String ip = client.getIP();
					if ((!ip.equals("127.0.0.1")) && BruteForceProtector.getInstance().addFailedConnect(ip)) {
						Timestamp newTime = new Timestamp(System.currentTimeMillis() + Config.WRONG_LOGIN_BAN_TIME * 60000);
						BannedIpController.banIp(ip, newTime);
						log.debug(user + " on " + ip + " banned for " + Config.WRONG_LOGIN_BAN_TIME + " min. bruteforce");
						client.close(new SM_LOGIN_FAIL(AionAuthResponse.BAN_IP), false);
						break;
					}
				}
				log.debug(user + " got invalid password attempt state");
				client.sendPacket(new SM_LOGIN_FAIL(response));
				break;
			default:
				log.debug(user + " got unknown (" + response.toString() + ") attempt state");
				client.close(new SM_LOGIN_FAIL(response), false);
				break;
		}
	}

	private String readStringFromBlock(Iterator<Byte> bytes) {
		StringBuilder sb = new StringBuilder();
		while (bytes.hasNext()) {
			char ch = (char) bytes.next().byteValue();
			if (ch == 0 || (byte) ch == -1) {
				if (sb.length() == 0)
					continue;
				else
					break;
			}
			sb.append(ch);
		}
		return sb.toString();
	}
}
