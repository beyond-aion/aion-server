package com.aionemu.gameserver.model.templates;

public class CraftLearnTemplate {

		private int skillId;
		private boolean isCraftSkill;

	/**
	 * @return the isCraftSkill
	 */
	public boolean isCraftSkill() {
		return isCraftSkill;
	}

	public CraftLearnTemplate(int skillId, boolean isCraftSkill, String skillName) {
		this.skillId = skillId;
		this.isCraftSkill = isCraftSkill;
	}

	/**
	 * @return the skillId
	 */
	public int getSkillId() {
		return skillId;
	}
}