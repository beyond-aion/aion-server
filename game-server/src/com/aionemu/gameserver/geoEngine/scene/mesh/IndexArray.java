package com.aionemu.gameserver.geoEngine.scene.mesh;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public interface IndexArray {

	int get(int i);
	int size();
	void swap(int i1, int i2);

	static IndexArray from(Buffer buffer) {
		return switch (buffer) {
			case ByteBuffer buf -> new IndexByteArray(buf);
			case ShortBuffer buf -> new IndexShortArray(buf);
			default -> throw new IllegalArgumentException(buffer.getClass().getSimpleName() + " is not supported");
		};
	}
}
