package com.aionemu.loginserver.network.ncrypt;

import com.aionemu.commons.utils.Rnd;

import io.netty.buffer.ByteBuf;

/**
 * Crypto engine for ecnrypting/decrypting packets, error handling and verifying checksum. Works on any {@link ByteBuf} (heap or direct) by
 * absolute index; the buffer's reader and writer index are not used.
 *
 * @author EvilSpirit
 */
public class CryptEngine {

	/**
	 * A key
	 */
	private byte[] key = { (byte) 0x6b, (byte) 0x60, (byte) 0xcb, (byte) 0x5b, (byte) 0x82, (byte) 0xce, (byte) 0x90, (byte) 0xb1, (byte) 0xcc,
		(byte) 0x2b, (byte) 0x6c, (byte) 0x55, (byte) 0x6c, (byte) 0x6c, (byte) 0x6c, (byte) 0x6c };
	/**
	 * Tells you whether the key is updated or not
	 */
	private boolean updatedKey = false;
	/**
	 * A secret blowfish cipher
	 */
	private BlowfishCipher cipher;

	/**
	 * Default constructor. Initialize the Blowfish Cipher with an initial static key to encrypt the first packet sent to the client
	 */
	public CryptEngine() {
		cipher = new BlowfishCipher(key);
	}

	/**
	 * Update the key for packet encryption/decryption with the Blowfish Cipher
	 *
	 * @param newKey
	 *          new Blowfish Key
	 */
	public void updateKey(byte[] newKey) {
		this.key = newKey;
	}

	/**
	 * Decrypt given data
	 *
	 * @param data
	 *          buffer to be decrypted
	 * @param offset
	 *          absolute index of the first byte
	 * @param length
	 *          number of bytes
	 * @return true, if decrypted packet has valid checksum, false overwise
	 */
	public boolean decrypt(ByteBuf data, int offset, int length) {
		cipher.decipher(data, offset, length);

		return verifyChecksum(data, offset, length);
	}

	/**
	 * Encrypt given data. The packet grows by its checksum and the padding to whole Blowfish blocks, so the buffer needs up to 12 more bytes after
	 * it.
	 *
	 * @param data
	 *          buffer to be encrypted
	 * @param offset
	 *          absolute index of the first byte
	 * @param length
	 *          number of bytes
	 * @return length of the encrypted packet
	 */
	public int encrypt(ByteBuf data, int offset, int length) {
		return encrypt(data, offset, length, Rnd.nextInt());
	}

	/**
	 * @param xorKey
	 *          key of the extra XOR pass over the first packet
	 */
	int encrypt(ByteBuf data, int offset, int length, int xorKey) {
		int dataLength = length;
		length += 4;

		// the key is not updated, so the first packet should be encrypted with
		// initial key
		if (!updatedKey) {
			length += 4;
			length += 8 - length % 8;
			clear(data, offset + dataLength, length - dataLength);
			encXORPass(data, offset, length, xorKey);
			cipher.cipher(data, offset, length);
			cipher.updateKey(key);
			updatedKey = true;
		} else {
			length += 8 - length % 8;
			clear(data, offset + dataLength, length - dataLength);
			appendChecksum(data, offset, length);
			cipher.cipher(data, offset, length);
		}

		return length;
	}

	/**
	 * Fills the space for the checksum and the padding with zeros, instead of leaving whatever the buffer held there before.
	 */
	private void clear(ByteBuf data, int from, int count) {
		for (int i = 0; i < count; i++)
			data.setByte(from + i, 0);
	}

	/**
	 * Verify checksum in a packet: the 32-bit words of the packet, except for the last one, must XOR to zero. That is what the former byte array
	 * version computed for a packet at the start of the read buffer; for packets further into the buffer its result depended on their position.
	 *
	 * @param data
	 *          decrypted packet
	 * @param offset
	 *          absolute index of the first byte
	 * @param length
	 *          number of bytes
	 * @return true, if checksum is ok, false overwise
	 */
	private boolean verifyChecksum(ByteBuf data, int offset, int length) {
		if ((length & 3) != 0 || (length <= 4)) {
			return false;
		}

		int chksum = 0;
		for (int i = 0; i < length - 4; i += 4)
			chksum ^= BlowfishCipher.getInt(data, offset + i);

		return 0 == chksum;
	}

	/**
	 * add checksum to the end of the packet
	 *
	 * @param raw
	 *          packet to be encrypted
	 * @param offset
	 *          absolute index of the first byte
	 * @param length
	 *          number of bytes, including the checksum
	 */
	private void appendChecksum(ByteBuf raw, int offset, int length) {
		int chksum = 0;
		int count = length - 4;

		for (int i = 0; i < count; i += 4)
			chksum ^= BlowfishCipher.getInt(raw, offset + i);

		BlowfishCipher.putInt(raw, offset + count, chksum);
	}

	/**
	 * First packet encryption with XOR key (integer - 4 bytes)
	 *
	 * @param data
	 *          packet to be encrypted
	 * @param offset
	 *          absolute index of the first byte
	 * @param length
	 *          number of bytes
	 * @param key
	 *          integer value as key
	 */
	private void encXORPass(ByteBuf data, int offset, int length, int key) {
		int stop = offset + length - 8;
		int pos = 4 + offset;
		int edx;
		int ecx = key;

		while (pos < stop) {
			edx = BlowfishCipher.getInt(data, pos);
			ecx += edx;
			edx ^= ecx;
			BlowfishCipher.putInt(data, pos, edx);
			pos += 4;
		}

		BlowfishCipher.putInt(data, pos, ecx);
	}
}
