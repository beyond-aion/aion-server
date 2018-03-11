package com.aionemu.gameserver.model.summons;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Rolandas, Neon
 */
public class SkillOrder {

	private final int skillId;
	private final int skillLvl;
	private final Creature target;
	private final int hate;
	private final boolean release;

	public SkillOrder(int skillId, int skillLvl, Creature target, int hate, boolean release) {
		this.skillId = skillId;
		this.skillLvl = skillLvl;
		this.target = target;
		// since no summon skills generate any hate and the order cast itself has hate values which are never broadcast, we assume that
		// the summon should broadcast that hate instead
		this.hate = hate;
		this.release = release;
	}

	public int getSkillId() {
		return skillId;
	}

	public int getSkillLevel() {
		return skillLvl;
	}

	public Creature getTarget() {
		return target;
	}

	public int getHate() {
		return hate;
	}

	public boolean isRelease() {
		return release;
	}

}
