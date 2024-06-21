package com.aionemu.loginserver.network.aion.clientpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
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

	private static final Logger log = LoggerFactory.getLogger(CM_LOGIN.class);

	/**
	 * Byte array containing RSA encrypted credentials. The last four bytes might be the OTP (-1 if not used, likely requested via AC_OTPCHECK_REQ)<br>
	 * <br>
	 * Starting the client without <code>-loginex</code> parameter, the username and pw are sent together in one 128 byte chunk. In this case, the
	 * username can be 14 characters long and the password 16<br>
	 * The offset where user and pw start seem to be fixed. Unused characters are zero padded, excessive characters are truncated.<br>
	 * Decrypted credentials example for user: abcdefghijklmn pw: abcdefghijklmnop
	 * 
	 * <pre>
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
	 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 61 62
	 * 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70 FF FF FF FF
	 * abcdefghijklmnabcdefghijklmnop????
	 * </pre>
	 * 
	 * Using <code>-loginex</code>, the username can be up to 64 characters long and the password 32. Both are sent split over two chunks<br>
	 * Decrypted chunk example for user: abcdefghijklmnopqrsabcdefghijklmnopqrsabcdefghijklmno3432432pqrs pw: 11111111111111111111111111111111
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
	private byte[] encryptedLoginData;
	private int sessionId;

	public CM_LOGIN(ByteBuffer buf, LoginConnection client, int opCode) {
		super(buf, client, opCode);
	}

	@Override
	protected void readImpl() {
		encryptedLoginData = readB(getRemainingBytes() - 55);
		sessionId = readD();
		readB(16); // 0
		readB(7); // static bytes: 20 00 00 00 00 00 01
		readB(16); // static bytes: 9D DA 47 A7 21 C0 A6 A5 4B B7 5E E3 CE C9 26 AA
		readD(); // 0
		readD(); // unk
		readD(); // 0
	}

	@Override
	@SuppressWarnings("fallthrough")
	protected void runImpl() {
		if (getConnection().getSessionId() != sessionId) {
			sendPacket(new SM_LOGIN_FAIL(AionAuthResponse.STR_L2AUTH_S_SYSTEM_ERROR));
			return;
		}
		LoginData loginData = decryptLoginData();
		if (loginData == null) {
			sendPacket(new SM_LOGIN_FAIL(AionAuthResponse.STR_L2AUTH_S_SYSTEM_ERROR));
			return;
		}
		LoginConnection client = getConnection();
		AionAuthResponse response = AccountController.login(loginData.username, loginData.password, client);
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
						log.info(loginData.username + " on " + ip + " banned for " + Config.WRONG_LOGIN_BAN_TIME + " min. bruteforce");
						client.close(new SM_LOGIN_FAIL(AionAuthResponse.STR_L2AUTH_S_BLOCKED_IP));
						break;
					}
				}
			default:
				client.sendPacket(new SM_LOGIN_FAIL(response));
				break;
		}
	}

	private LoginData decryptLoginData() {
		byte[] decrypted = new byte[encryptedLoginData.length];
		try {
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, getConnection().getRSAPrivateKey());
			for (int offset = 0; offset < encryptedLoginData.length; offset += 128)
				rsaCipher.doFinal(encryptedLoginData, offset, 128, decrypted, offset);
		} catch (GeneralSecurityException ignored) {
			return null;
		}
		boolean isLoginEx = encryptedLoginData.length > 128; // client was started with -loginex parameter
		int contentStartOffset = isLoginEx ? 78 : 94; // decrypted chunks start with the same zero-padding
		int usernameByteLength = isLoginEx ? 64 : 14;
		int passwordByteLength = isLoginEx ? 32 : 16; // we can't detect -pwd16 (also limits to 16 characters), but handling zero-termination is sufficient
		for (int offset = encryptedLoginData.length - 128; offset >= 0; offset -= 128) // shift to the left to remove prefix zero-padding
			System.arraycopy(decrypted, offset + contentStartOffset, decrypted, offset, decrypted.length - (offset + contentStartOffset));
		Charset charset = Charset.forName("Cp1252"); // should be sent by the client, but so far it's only been found in CM_VERSION_CHECK
		String username = readString(decrypted, 0, usernameByteLength, charset);
		String password = readString(decrypted, usernameByteLength, passwordByteLength, charset);
		int otp = ByteBuffer.wrap(decrypted, usernameByteLength + passwordByteLength, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
		return new LoginData(username, password, otp);
	}

	private String readString(byte[] bytes, int offset, int maxLength, Charset charset) {
		int length = 0;
		for (int i = offset; length < maxLength && i < bytes.length && bytes[i] != 0; i++)
			length++;
		return new String(bytes, offset, length, charset);
	}

	record LoginData(String username, String password, int otp) {}
}
