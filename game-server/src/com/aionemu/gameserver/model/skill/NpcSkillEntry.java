package com.aionemu.gameserver.model.skill;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillConditionTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;

/**
 * @author ATracer, nrg, Yeats
 */
public abstract class NpcSkillEntry extends SkillEntry {

	public NpcSkillEntry(int skillId, int skillLevel) {
		super(skillId, skillLevel);
	}

	public abstract boolean isReady(int hpPercentage, long fightingTimeInMSec);

	public abstract boolean chanceReady();

	public abstract boolean hpReady(int hpPercentage);

	public abstract boolean timeReady(long fightingTimeInMSec);

	public abstract boolean hasPostSpawnCondition();

	public abstract int getPriority();
	
	public abstract boolean conditionReady(Creature creature);
	
	public abstract NpcSkillConditionTemplate getConditionTemplate();
	
	public abstract boolean hasCondition();

	public abstract int getNextSkillTime();
	
	public abstract boolean hasChain();
	
	public abstract int getNextChainId();
	
	public abstract int getChainId();

	public abstract boolean canUseNextChain(Npc owner);

	public abstract NpcSkillTemplate getTemplate();

	public abstract void fireOnEndCastEvents(Npc npc);

}

