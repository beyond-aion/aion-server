package com.aionemu.gameserver.utils.xml;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author Rolandas
 */
public final class CompressUtil {

	public static String decompress(byte[] bytes) throws Exception {
		Inflater decompressor = new Inflater();
		decompressor.setInput(bytes);

		// Create an expandable byte array to hold the decompressed data
		ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);

		byte[] buffer = new byte[1024];
		try {
			while (true) {
				int count = decompressor.inflate(buffer);
				if (count > 0) {
					bos.write(buffer, 0, count);
				} else if (count == 0 && decompressor.finished()) {
					break;
				} else {
					throw new RuntimeException("Bad zip data, size: " + bytes.length);
				}
			}
		} finally {
			decompressor.end();
		}

		return bos.toString(StandardCharsets.UTF_16LE);
	}

	public static byte[] compress(String text) {
		Deflater compressor = new Deflater();
		byte[] bytes = text.getBytes(StandardCharsets.UTF_16LE);
		compressor.setInput(bytes);

		// Create an expandable byte array to hold the compressed data
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		compressor.finish();

		byte[] buffer = new byte[1024];
		try {
			while (!compressor.finished()) {
				int count = compressor.deflate(buffer);
				bos.write(buffer, 0, count);
			}
		} finally {
			compressor.finish();
		}

		return bos.toByteArray();
	}
}
