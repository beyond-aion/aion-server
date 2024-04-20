package com.aionemu.gameserver.network;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.network.aion.serverpackets.SM_VERSION_CHECK;

/**
 * Crypt will encrypt server packet and decrypt client packet.
 * 
 * @author hack99, kao, -Nemesiss-
 */
public class Crypt {

	private final static Logger log = LoggerFactory.getLogger(Crypt.class);

	/**
	 * Second byte of server packet must be equal to this
	 */
	public final static byte staticServerPacketCode = 0x44;

	/**
	 * Crypt is enabled after first server packet was send.
	 */
	private boolean isEnabled;

	private EncryptionKeyPair packetKey = null;

	/**
	 * Enable crypt key - generate random key that will be used to encrypt second server packet [first one is unencrypted] and decrypt client packets.
	 * This method is called from SM_KEY server packet, that packet sends key to aion client.
	 * 
	 * @return Enciphered key which the client will decipher and use for all future (encrypted) communication
	 */
	public final int enableKey() {
		if (packetKey != null)
			throw new IllegalStateException("Key is already initialized");

		// rnd key - this will be used to encrypt/decrypt packets
		int key = Rnd.nextInt();

		packetKey = new EncryptionKeyPair(key);

		log.debug("new encrypt key: {}", packetKey);

		// enciphered key that will be sent to aion client in SM_KEY packet
		return (key ^ 0xCD92E4DF) + 0x3FF2CCCF;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Decrypt client packet from this ByteBuffer.
	 * 
	 * @return true if decryption was successful.
	 */
	public final boolean decrypt(ByteBuffer buf) {
		return packetKey.decrypt(buf);
	}

	/**
	 * Encrypt server packet from this ByteBuffer.
	 */
	public final void encrypt(ByteBuffer buf) {
		if (!isEnabled) {
			// first server packet (SM_KEY) is not encrypted because it sends the enciphered crypt key
			isEnabled = true;
			return;
		}

		packetKey.encrypt(buf);
	}

	/**
	 * @return Obfuscated server packet opcode
	 */
	public static int encodeServerPacketOpcode(int opcode) {
		return (opcode + SM_VERSION_CHECK.INTERNAL_VERSION) ^ 0xDF;
	}

	/**
	 * @return Deobfuscated client packet opcode
	 */
	public static int decodeClientPacketOpcode(int opcode) {
		return ((opcode ^ 0xEF) - 0xC ^ 0xEF) - SM_VERSION_CHECK.INTERNAL_VERSION;
	}
}
