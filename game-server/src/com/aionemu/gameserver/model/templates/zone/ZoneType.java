package com.aionemu.gameserver.model.templates.zone;


/**
 * @author MrPoke
 *
 */
public enum ZoneType {
	
	FLY(0),
	DAMAGE(1),
	WATER(2),
	SIEGE(3),
	PVP(4);
	
	private byte value;

	/**
	 * @param value
	 */
	private ZoneType(int value) {
		this.value = (byte)value;
	}
	
	/**
	 * @return the value
	 */
	public byte getValue() {
		return value;
	}
}
