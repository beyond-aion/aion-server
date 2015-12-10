package com.aionemu.gameserver.model.skill;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillCondition;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillConditionTemplate;

/**
 * @author ATracer, nrg
 * @reworked Yeats
 */
public abstract class NpcSkillEntry extends SkillEntry {

	protected long lastTimeUsed = 0;

	public NpcSkillEntry(int skillId, int skillLevel) {
		super(skillId, skillLevel);
	}

	public abstract boolean isReady(int hpPercentage, long fightingTimeInMSec);

	public abstract boolean chanceReady();

	public abstract boolean hpReady(int hpPercentage);

	public abstract boolean timeReady(long fightingTimeInMSec);

	public abstract boolean hasCooldown();

	public abstract boolean UseInSpawned();

	public long getLastTimeUsed() {
		return lastTimeUsed;
	}

	public void setLastTimeUsed() {
		this.lastTimeUsed = System.currentTimeMillis();
	}
	
	public abstract int getPriority();
	
	public abstract boolean conditionReady(Creature creature);
	
	public abstract NpcSkillCondition getCondition();
	
	public abstract NpcSkillConditionTemplate getConditionTemplate();
	
	public abstract boolean hasCondition();
	
	public abstract int getRange();
	
	public abstract int getHpBelow();

	public abstract int getNextSkillTime();
	
}

