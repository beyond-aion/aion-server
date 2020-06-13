package com.aionemu.loginserver.network.aion.clientpackets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;

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

/**
 * @author -Nemesiss-, KID, Lyahim, Rolandas, Neon
 */
public class CM_LOGIN extends AionClientPacket {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(CM_LOGIN.class);

	/**
	 * Byte arrays containing RSA encrypted user name and password. These parts are 128 bytes long and currently are known to appear in a quantity of
	 * one or two (depending on the -loginex parameter).<br>
	 * <br>
	 * Starting the client without -loginex parameter, the username and pw are sent together in one dataPart. In this case, the user name can be 14
	 * characters long and the password 16<br>
	 * The offset where user and pw start seem to be fixed. Unused characters are zero padded, excessive characters are truncated.<br>
	 * The last part always ends with 4 0xFF bytes (after decryption)<br>
	 * Decrypted dataPart example for user: abcdefghijklmn pw: abcdefghijklmnop
	 * 
	 * <pre>
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 61 62
	 * 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70 FF FF FF FF
	 * abcdefghijklmnabcdefghijklmnop????
	 * </pre>
	 * 
	 * Using -loginex, the username can be up to 64 characters long and the password 32. Both are sent split over two dataParts<br>
	 * Decrypted dataPart example for user: abcdefghijklmnopqrsabcdefghijklmnopqrsabcdefghijklmno3432432pqrs pw: 11111111111111111111111111111111
	 * 
	 * <pre>
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70 71 72 
	 * 73 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70 71 72 73 61 62 63 64 65 66 67 68 69 6A 6B 6C
	 * abcdefghijklmnopqrsabcdefghijklmnopqrsabcdefghijkl
	 * </pre>
	 * 
	 * <pre>
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6D 6E 6F 33 34 33 32 34 33 32 70 71 72 73 31 31 31 31 
	 * 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 FF FF FF FF
	 * mno3432432pqrs11111111111111111111111111111111????
	 * </pre>
	 */
	private byte[][] dataParts;
	private int sessionId;

	/**
	 * Constructs new instance of <tt>CM_LOGIN</tt> packet.
	 * 
	 * @param buf
	 * @param client
	 */
	public CM_LOGIN(ByteBuffer buf, LoginConnection client, int opCode) {
		super(buf, client, opCode);
	}

	@Override
	protected void readImpl() {
		int dataPartCount = (getRemainingBytes() - 55) / 128;
		dataParts = new byte[dataPartCount][];
		for (int i = 0; i < dataPartCount; i++)
			dataParts[i] = readB(128);
		sessionId = readD();
		readB(16); // 0
		readB(7); // static bytes: 20 00 00 00 00 00 01
		readB(16); // static bytes: 9D DA 47 A7 21 C0 A6 A5 4B B7 5E E3 CE C9 26 AA
		int d1 = readD(); // 0
		int d2 = readD(); // unk
		int d3 = readD(); // 0
		System.out.println("CM_LOGIN: d1=" + d1 + " d2=" + d2 + " d3=" + d3);
	}

	@Override
	@SuppressWarnings("fallthrough")
	protected void runImpl() {
		if (getConnection().getSessionId() != sessionId) {
			sendPacket(new SM_LOGIN_FAIL(AionAuthResponse.STR_L2AUTH_S_SYSTEM_ERROR));
			return;
		}

		String decrypted = "";
		try {
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, getConnection().getRSAPrivateKey());
			int leadingNulls = -1;
			for (byte[] dataPart : dataParts) {
				String decryptedDataPart = new String(rsaCipher.doFinal(dataPart), StandardCharsets.UTF_8);
				if (leadingNulls == -1) // init once, since subsequent parts may also have \u0000's where the end of very long user names would be
					leadingNulls = decryptedDataPart.indexOf(decryptedDataPart.trim());
				decrypted += decryptedDataPart.substring(leadingNulls);
			}
			decrypted = decrypted.substring(0, decrypted.indexOf(0xFFFD));
		} catch (GeneralSecurityException e) {
			sendPacket(new SM_LOGIN_FAIL(AionAuthResponse.STR_L2AUTH_S_SYSTEM_ERROR));
			return;
		}

		int userNameLength = dataParts.length == 1 ? 14 : 64; // if more than 1 dataParts, -loginex parameter was used (long user name / pw support)
		String userName = decrypted.substring(0, userNameLength).trim();
		String password = decrypted.substring(userNameLength).trim();

		LoginConnection client = getConnection();
		AionAuthResponse response = AccountController.login(userName, password, client);
		if (response == null) // e.g. when account is banned
			return;
		switch (response) {
			case STR_L2AUTH_S_ALL_OK:
				client.setState(State.AUTHED_LOGIN);
				client.setSessionKey(new SessionKey(client.getAccount()));
				client.sendPacket(new SM_LOGIN_OK(client.getSessionKey()));
				break;
			case STR_L2AUTH_S_INVALID_ACCOUT:
			case STR_L2AUTH_S_INCORRECT_PWD:
				if (Config.ENABLE_BRUTEFORCE_PROTECTION) {
					String ip = client.getIP();
					if (!ip.equals("127.0.0.1") && BruteForceProtector.getInstance().addFailedConnect(ip)) {
						Timestamp newTime = new Timestamp(System.currentTimeMillis() + Config.WRONG_LOGIN_BAN_TIME * 60000);
						BannedIpController.banIp(ip, newTime);
						log.info(userName + " on " + ip + " banned for " + Config.WRONG_LOGIN_BAN_TIME + " min. bruteforce");
						client.close(new SM_LOGIN_FAIL(AionAuthResponse.STR_L2AUTH_S_BLOCKED_IP));
						break;
					}
				}
			default:
				client.sendPacket(new SM_LOGIN_FAIL(response));
				break;
		}
	}
}
