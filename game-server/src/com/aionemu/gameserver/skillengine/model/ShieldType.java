package com.aionemu.gameserver.skillengine.model;


/**
 * @author kecimis
 *
 */
public enum ShieldType {
	/**
	 * shieldType
	 * 0: convertHeal
	 * 1: reflector
	 * 2: normal shield
	 * 8: protect
	 * 16: mp shield
	 */
	CONVERT(0),
	REFLECTOR(1 << 0),//1
	NORMAL(1 << 1),//2
	UNK(1 << 2),//4
	PROTECT(1 << 3),//8
	MPSHIELD(1 << 4);//16

	private int id;

	private ShieldType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
