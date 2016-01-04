package com.aionemu.gameserver.model.templates.item;

public class StigmaSkill {

	private int skillId;
	private int skillLvl;
	private boolean isLinkedStigma;

	public StigmaSkill(int skillLvl, int skillId, boolean isLinkedStigma) {
		this.skillId = skillId;
		this.skillLvl = skillLvl;
		this.isLinkedStigma = isLinkedStigma;
	}

	public int getSkillLvl() {
		return this.skillLvl;
	}

	public int getSkillId() {
		return this.skillId;
	}

	public boolean isLinkedStigma() {
		return this.isLinkedStigma;
	}
}
