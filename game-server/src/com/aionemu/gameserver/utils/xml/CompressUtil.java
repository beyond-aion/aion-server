package com.aionemu.gameserver.utils.xml;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author Rolandas
 */
public final class CompressUtil {

	public static byte[] decompress(byte[] bytes) throws Exception {
		// Create an expandable byte array to hold the decompressed data
		ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);

		byte[] buffer = new byte[1024];
		try (Inflater decompressor = new Inflater()) {
			decompressor.setInput(bytes);
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
		}

		return bos.toByteArray();
	}

	public static byte[] compress(byte[] bytes) {
		// Create an expandable byte array to hold the compressed data
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		try (Deflater compressor = new Deflater()) {
			compressor.setInput(bytes);
			compressor.finish();
			while (!compressor.finished()) {
				int count = compressor.deflate(buffer);
				bos.write(buffer, 0, count);
			}
		}

		return bos.toByteArray();
	}
}
