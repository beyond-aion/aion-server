package com.aionemu.gameserver.model.skill;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
public abstract class SkillEntry {

	protected final int skillId;

	protected int skillLevel;

	SkillEntry(int skillId, int skillLevel) {
		this.skillId = skillId;
		this.skillLevel = skillLevel;
	}

	public final int getSkillId() {
		return skillId;
	}

	public final int getSkillLevel() {
		return skillLevel;
	}

	public final String getSkillName() {
		return DataManager.SKILL_DATA.getSkillTemplate(getSkillId()).getName();
	}

	public void setSkillLvl(int skillLevel) {
		this.skillLevel = skillLevel;
	}

	public final SkillTemplate getSkillTemplate() {
		return DataManager.SKILL_DATA.getSkillTemplate(getSkillId());
	}

}
