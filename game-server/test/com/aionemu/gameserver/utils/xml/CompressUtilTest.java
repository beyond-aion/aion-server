package com.aionemu.gameserver.utils.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Pins how the client's script block is measured: the stream ends before the 8 trailing bytes the client keeps from our packets.
 */
class CompressUtilTest {

	private static final byte[] SCRIPT = """
		<?xml version="1.0" encoding="UTF-16" ?>
		<lboxes>
			<lbox>
				<id>1</id>
				<name><![CDATA[Test]]></name>
			</lbox>
		</lboxes>
		""".getBytes(StandardCharsets.UTF_16LE);

	@Test
	void measuresTheWholeStream() {
		byte[] compressed = CompressUtil.compress(SCRIPT);
		assertEquals(compressed.length, CompressUtil.zlibStreamLength(compressed, SCRIPT.length));
	}

	@Test
	void ignoresTheTrailingBytesOfTheClientBlock() {
		byte[] compressed = CompressUtil.compress(SCRIPT);
		byte[] block = Arrays.copyOf(compressed, compressed.length + 8);
		for (int i = compressed.length; i < block.length; i++)
			block[i] = (byte) 0xCD;
		assertEquals(compressed.length, CompressUtil.zlibStreamLength(block, SCRIPT.length));
	}

	@Test
	void rejectsAWrongSize() {
		byte[] compressed = CompressUtil.compress(SCRIPT);
		assertEquals(-1, CompressUtil.zlibStreamLength(compressed, SCRIPT.length + 2));
		assertEquals(-1, CompressUtil.zlibStreamLength(compressed, SCRIPT.length - 2));
	}

	@Test
	void rejectsATruncatedStream() {
		byte[] compressed = CompressUtil.compress(SCRIPT);
		assertEquals(-1, CompressUtil.zlibStreamLength(Arrays.copyOf(compressed, compressed.length / 2), SCRIPT.length));
	}
}
