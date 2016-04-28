package com.aionemu.gameserver.model.skill;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillConditionTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;

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
	public NpcSkillConditionTemplate getConditionTemplate() {
		return null;
	}

	@Override
	public boolean hasCondition() {
		return false;
	}

	@Override
	public int getNextSkillTime() {
		return -1;
	}
	
	@Override
	public boolean hasChain() {
		return false;
	}

	@Override
	public int getNextChainId() {
		return 0;
	}
	
	@Override
	public int getChainId() {
		return 0;
	}
	
	@Override
	public boolean canUseNextChain(Npc owner) {
		return false;
	}
	
	@Override
	public NpcSkillTemplate getTemplate() {
		return null;
	}

	@Override
	public void fireOnEndCastEvents(Npc npc) {
	}

	@Override
	public void fireOnStartCastEvents(Npc npc) {

	}

	@Override
	public boolean isQueued() {
		return false;
	}

	@Override
	public boolean ignoreNextSkillTime() {
		return false;
	}
}
