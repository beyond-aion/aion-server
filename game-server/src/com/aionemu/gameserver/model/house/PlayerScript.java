package com.aionemu.gameserver.model.house;

import com.aionemu.commons.taskmanager.AbstractLockManager;

/**
 * @author Rolandas
 * Use readLock + readUnlock to read both fields, because they may change !!!
 */
public final class PlayerScript extends AbstractLockManager {

	/**
	 * Default constructor, bytes null, uncompressed size -1
	 */
	public PlayerScript() {

	}

	public PlayerScript(byte[] compressedBytes, int uncompressedSize) {
		this.compressedBytes = compressedBytes;
		this.uncompressedSize = uncompressedSize;
	}

	private int uncompressedSize = -1;
	private byte[] compressedBytes = null;

	public int getUncompressedSize() {
		return uncompressedSize;
	}

	public byte[] getCompressedBytes() {
		return compressedBytes;
	}

	public void setData(byte[] compressedBytes, int uncompressedSize) {
		writeLock();
		this.compressedBytes = compressedBytes;
		this.uncompressedSize = uncompressedSize;
		writeUnlock();
	}
}
