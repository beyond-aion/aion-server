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
	 * @return True if the bytes are a complete zlib stream that inflates to exactly the given size. Nothing is kept of the inflated data.
	 */
	public static boolean inflatesTo(byte[] bytes, int size) {
		byte[] buffer = new byte[1024];
		long inflatedSize = 0;
		try (Inflater decompressor = new Inflater()) {
			decompressor.setInput(bytes);
			while (!decompressor.finished()) {
				int count = decompressor.inflate(buffer);
				if (count == 0 && (decompressor.needsInput() || decompressor.needsDictionary()))
					return false;
				inflatedSize += count;
				if (inflatedSize > size)
					return false;
			}
			return inflatedSize == size && decompressor.getRemaining() == 0;
		} catch (DataFormatException e) {
			return false;
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
