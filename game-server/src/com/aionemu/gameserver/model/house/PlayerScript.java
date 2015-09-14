package com.aionemu.gameserver.model.house;

/**
 * @author Rolandas
 * @modified Neon
 */
public final class PlayerScript {

	private byte[] compressedBytes;
	private int uncompressedSize;

	public PlayerScript(byte[] compressedBytes, int uncompressedSize) {
		this.compressedBytes = compressedBytes;
		this.uncompressedSize = uncompressedSize;
	}

	public int getUncompressedSize() {
		return uncompressedSize;
	}

	public byte[] getCompressedBytes() {
		return compressedBytes;
	}
}
