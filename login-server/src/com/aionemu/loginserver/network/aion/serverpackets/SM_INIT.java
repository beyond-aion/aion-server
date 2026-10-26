package com.aionemu.loginserver.network.aion.serverpackets;

import javax.crypto.SecretKey;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

public final class SM_INIT extends AionServerPacket {

	/**
	 * Session Id of this connection
	 */
	private final int sessionId;

	/**
	 * public Rsa key that client will use to encrypt login and password that will be send in RequestAuthLogin client packet.
	 */
	private final byte[] publicRsaKey;
	/**
	 * blowfish key for packet encryption/decryption.
	 */
	private final byte[] blowfishKey;

	public SM_INIT(LoginConnection client, SecretKey blowfishKey) {
		this(client.getEncryptedModulus(), blowfishKey.getEncoded(), client.getSessionId());
	}

	private SM_INIT(byte[] publicRsaKey, byte[] blowfishKey, int sessionId) {
		super(0x00);
		this.sessionId = sessionId;
		this.publicRsaKey = publicRsaKey;
		this.blowfishKey = blowfishKey;
	}

	/**
	 * Protocol revision 50721: the 4.8 client picks its packet encryption by it (it also knows 30206, 30810 and 40916, with other ciphers).
	 */
	private static final int PROTOCOL_REVISION = 0xC621;

	@Override
	protected void writeImpl(LoginConnection con) {
		writeD(sessionId);
		writeD(PROTOCOL_REVISION);
		writeB(publicRsaKey); // scrambled RSA modulus, 128 bytes
		writeB(new byte[16]); // skipped by the client
		writeB(blowfishKey); // 16 bytes
		writeC(0); // login mode: 0 password; with 3 or 4 the client reads 3 more bytes and logs in with an external token
	}
}
