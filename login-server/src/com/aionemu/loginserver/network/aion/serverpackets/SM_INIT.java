package com.aionemu.loginserver.network.aion.serverpackets;

import javax.crypto.SecretKey;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

/**
 * Format: dd b dddd s d: session id d: protocol revision b: 0x90 bytes : 0x80 bytes for the scrambled RSA public key
 * 0x10 bytes at 0x00 d: unknow d: unknow d: unknow d: unknow s: blowfish key
 */
public final class SM_INIT extends AionServerPacket {

	/**
	 * Session Id of this connection
	 */
	private final int sessionId;

	/**
	 * public Rsa key that client will use to encrypt login and password that will be send in RequestAuthLogin client
	 * packet.
	 */
	private final byte[] publicRsaKey;
	/**
	 * blowfish key for packet encryption/decryption.
	 */
	private final byte[] blowfishKey;

	/**
	 * Constructor
	 * 
	 * @param client
	 * @param blowfishKey
	 */
	public SM_INIT(LoginConnection client, SecretKey blowfishKey) {
		this(client.getEncryptedModulus(), blowfishKey.getEncoded(), client.getSessionId());
	}

	/**
	 * Creates new instance of <tt>SM_INIT</tt> packet.
	 * 
	 * @param publicRsaKey
	 *          Public RSA key
	 * @param blowfishKey
	 *          Blowfish key
	 * @param sessionId
	 *          Session identifier
	 */
	private SM_INIT(byte[] publicRsaKey, byte[] blowfishKey, int sessionId) {
		super(0x00);
		this.sessionId = sessionId;
		this.publicRsaKey = publicRsaKey;
		this.blowfishKey = blowfishKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(LoginConnection con) {
		writeD(sessionId); // session id
		writeD(0x0000c621); // protocol revision
		writeB(publicRsaKey); // RSA Public Key, 128 bytes
		// unk
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);

		writeB(blowfishKey); // BlowFish key, 16 bytes
		writeD(0); // unk
		writeB(new byte[11]); // unk
		writeD(0xD98E9655); // unk
		writeD(0); // unk
	}
}
