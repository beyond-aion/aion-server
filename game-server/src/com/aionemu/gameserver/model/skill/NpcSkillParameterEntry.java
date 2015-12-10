package com.aionemu.gameserver.model.skill;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillCondition;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillConditionTemplate;

/**
 * Skill entry which can be created on the fly (skills of servants, traps)
 * 
 * @author ATracer, nrg
 * @reworked Yeats
 */
public class NpcSkillParameterEntry extends NpcSkillEntry {

	public NpcSkillParameterEntry(int skillId, int skillLevel) {
		super(skillId, skillLevel);
	}

	@Override
	public boolean isReady(int hpPercentage, long fightingTimeInMSec) {
		return true;
	}

	@Override
	public boolean chanceReady() {
		return true;
	}

	@Override
	public boolean hpReady(int hpPercentage) {
		return true;
	}

	@Override
	public boolean timeReady(long fightingTimeInMSec) {
		return true;
	}

	@Override
	public boolean hasCooldown() {
		return false;
	}

	@Override
	public boolean UseInSpawned() {
		return true;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public boolean conditionReady(Creature creature) {
		return true;
	}

	@Override
	public NpcSkillCondition getCondition() {
		return NpcSkillCondition.NONE;
	}

	@Override
	public NpcSkillConditionTemplate getConditionTemplate() {
		return null;
	}

	@Override
	public boolean hasCondition() {
		return false;
	}

	@Override
	public int getRange() {
		return 0;
	}

	@Override
	public int getHpBelow() {
		return 0;
	}

	@Override
	public int getNextSkillTime() {
		return -1;
	}
}
