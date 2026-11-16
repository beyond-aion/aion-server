package com.aionemu.gameserver.utils.xml;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author Rolandas
 */
public final class CompressUtil {

	/**
	 * @return Length of the zlib stream at the start of the bytes if it inflates to exactly {@code size}, otherwise -1.
	 */
	public static int zlibStreamLength(byte[] bytes, int size) {
		byte[] buffer = new byte[1024];
		long inflatedSize = 0;
		try (Inflater decompressor = new Inflater()) {
			decompressor.setInput(bytes);
			while (!decompressor.finished()) {
				int count = decompressor.inflate(buffer);
				if (count == 0 && (decompressor.needsInput() || decompressor.needsDictionary()))
					return -1;
				inflatedSize += count;
				if (inflatedSize > size)
					return -1;
			}
			return inflatedSize == size ? bytes.length - decompressor.getRemaining() : -1;
		} catch (DataFormatException e) {
			return -1;
		}
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
