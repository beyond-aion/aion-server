package com.aionemu.gameserver.geoEngine.scene.mesh;

import java.nio.ShortBuffer;

import com.aionemu.gameserver.geoEngine.utils.TempVars;

public record IndexShortArray(short[] buf) implements IndexArray {

	public IndexShortArray(ShortBuffer buf) {
		this(new short[buf.limit()]);
		buf.get(this.buf);
	}

	@Override
	public int get(int i) {
		return buf[i] & 0xFFFF;
	}

	@Override
	public int size() {
		return buf.length;
	}

	@Override
	public void swap(int i1, int i2) {
		int p1 = i1 * 3;
		int p2 = i2 * 3;
		TempVars vars = TempVars.get();
		// store p1 in tmp
		System.arraycopy(buf, p1, vars.bihSwapTmpShort, 0, 3);

		// copy p2 to p1
		System.arraycopy(buf, p2, buf, p1, 3);

		// copy tmp to p2
		System.arraycopy(vars.bihSwapTmpShort, 0, buf, p2, 3);
		vars.release();
	}
}
