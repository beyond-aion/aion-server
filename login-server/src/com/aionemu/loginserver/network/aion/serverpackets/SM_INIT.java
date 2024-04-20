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

	@Override
	protected void writeImpl(LoginConnection con) {
		writeD(sessionId); // session id
		writeD(0x0000c621); // protocol revision
		writeB(publicRsaKey); // RSA Public Key, 128 bytes
		writeB(new byte[16]); // 0, spacer?
		writeB(blowfishKey); // BlowFish key, 16 bytes
		writeB(new byte[7]); // 0, spacer?
		writeC(0); // test server id (100)
		writeD(0); // test server ip (1632857679 = 79.110.83.97)
		writeH(0); // test server port (7777)
		writeC(0); // 0, flag?
		writeD(0x3FCE09ED); // unk (old 0xD98E9655)
		writeD(0); // unk
	}
}
