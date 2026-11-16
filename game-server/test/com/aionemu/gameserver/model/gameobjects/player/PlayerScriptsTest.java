package com.aionemu.gameserver.model.gameobjects.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.utils.xml.CompressUtil;

/**
 * Pins the script block limits and the client's trailing bytes.
 */
class PlayerScriptsTest {

	private static final byte[] XML = "<lboxes/>".repeat(300).getBytes(StandardCharsets.UTF_16LE);

	@Test
	void acceptsTheClientBlockAndDropsItsTrailingBytes() {
		byte[] compressed = CompressUtil.compress(XML);
		byte[] block = Arrays.copyOf(compressed, compressed.length + 8);
		for (int i = compressed.length; i < block.length; i++)
			block[i] = (byte) 0xCD;

		PlayerScripts scripts = new PlayerScripts(1);
		assertTrue(scripts.set(0, block, XML.length, false));
		assertEquals(compressed.length, scripts.get(0).compressedBytes().length);
	}

	@Test
	void rejectsAWrongOrOversizedUncompressedSize() {
		byte[] compressed = CompressUtil.compress(XML);
		PlayerScripts scripts = new PlayerScripts(1);
		assertFalse(scripts.set(0, compressed, XML.length + 2, false));
		assertFalse(scripts.set(0, compressed, -1, false));
		assertFalse(scripts.set(0, compressed, PlayerScripts.MAX_UNCOMPRESSED_SCRIPT_SIZE + 1, false));
	}

	@Test
	void rejectsContentLargerThanTheClientBlock() {
		byte[] block = new byte[PlayerScripts.MAX_SCRIPT_BLOCK_SIZE - 7]; // one more than the client's compressedSize can be
		PlayerScripts scripts = new PlayerScripts(1);
		assertFalse(scripts.set(0, block, XML.length, false));
	}
}
