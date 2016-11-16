package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 * @modified Cheatkiller, Neon
 */
@XmlType(name = "TargetSlot")
@XmlEnum
public enum SkillTargetSlot {
	BUFF(1),
	DEBUFF(2),
	CHANT(4),
	SPEC(8),
	SPEC2(16), // soul sickness
	SPECIAL2(16), // soul sickness dispel
	BOOST(32),
	NOSHOW(64),
	NONE(128);

	private int id;

	public static final int FULLSLOTS = 127;

	private SkillTargetSlot(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
