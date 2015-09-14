package com.aionemu.gameserver.model.skill;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;

/**
 * @author ATracer, nrg
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
}

/**
 * Skill entry which inherits properties from template (regular npc skills)
 */
class NpcSkillTemplateEntry extends NpcSkillEntry {

	private final NpcSkillTemplate template;

	public NpcSkillTemplateEntry(NpcSkillTemplate template) {
		super(template.getSkillid(), template.getSkillLevel());
		this.template = template;
	}

	@Override
	public boolean isReady(int hpPercentage, long fightingTimeInMSec) {
		if (hasCooldown() || !chanceReady())
			return false;

		switch (template.getConjunctionType()) {
			case XOR:
				return (hpReady(hpPercentage) && !timeReady(fightingTimeInMSec)) || (!hpReady(hpPercentage) && timeReady(fightingTimeInMSec));
			case OR:
				return hpReady(hpPercentage) || timeReady(fightingTimeInMSec);
			case AND:
				return hpReady(hpPercentage) && timeReady(fightingTimeInMSec);
			default:
				return false;
		}
	}

	@Override
	public boolean chanceReady() {
		return Rnd.get(0, 100) < template.getProbability();
	}

	@Override
	public boolean hpReady(int hpPercentage) {
		if (template.getMaxhp() == 0 && template.getMinhp() == 0) // it's not about hp
			return true;
		else if (template.getMaxhp() >= hpPercentage && template.getMinhp() <= hpPercentage) // in hp range
			return true;
		else
			return false;
	}

	@Override
	public boolean timeReady(long fightingTimeInMSec) {
		if (template.getMaxTime() == 0 && template.getMinTime() == 0) // it's not about time
			return true;
		else if (template.getMaxTime() >= fightingTimeInMSec && template.getMinTime() <= fightingTimeInMSec) // in time range
			return true;
		else
			return false;
	}

	@Override
	public boolean hasCooldown() {
		return template.getCooldown() > (System.currentTimeMillis() - lastTimeUsed);
	}

	@Override
	public boolean UseInSpawned() {
		return template.getUseInSpawned();
	}
}

/**
 * Skill entry which can be created on the fly (skills of servants, traps)
 */
class NpcSkillParameterEntry extends NpcSkillEntry {

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
}
