package com.aionemu.gameserver.model.templates.npc;

import jakarta.xml.bind.annotation.XmlEnum;

/**
 * Share of the reward that is granted to the attacker who dealt the first hit, the rest is split by damage.
 */
@XmlEnum
public enum FirstAttackSplitRate {

	Split_None(0),
	Split_Little(0.05f),
	Split_Default(0.1f),
	Split_Much(0.25f),
	Split_Extra(0.51f);

	private final float rate;

	FirstAttackSplitRate(float rate) {
		this.rate = rate;
	}

	public float getRate() {
		return rate;
	}
}
