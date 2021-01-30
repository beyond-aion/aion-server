package com.aionemu.gameserver.model.house;

/**
 * @author Rolandas, Neon, Sykra
 */
public record PlayerScript(int id, byte[] compressedBytes, int uncompressedSize) {

	public boolean hasData() {
		return compressedBytes != null && compressedBytes.length > 0;
	}

}
