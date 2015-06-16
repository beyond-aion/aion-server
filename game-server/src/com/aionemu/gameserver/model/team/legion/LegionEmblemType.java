package com.aionemu.gameserver.model.team.legion;

/**
 * @author cura
 */
public enum LegionEmblemType {
	DEFAULT(0x00),
	CUSTOM(0x80);

	private byte value;

	private LegionEmblemType(int value) {
		this.value = (byte) value;
	}

	public byte getValue() {
		return value;
	}
}
