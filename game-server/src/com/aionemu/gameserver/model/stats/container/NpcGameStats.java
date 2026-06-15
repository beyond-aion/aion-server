package com.aionemu.gameserver.model.stats.container;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.stats.StatsTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author xavier, Estrayl
 */
public class NpcGameStats extends CreatureGameStats<Npc> {

	private long lastAttackTime = 0;
	private long lastAttackedTime = 0;
	private long nextAttackTime = 0;
	private long lastSkillTime = 0;
	private int nextSkillDelay = 0;
	private NpcSkillEntry lastSkill = null;
	private long fightStartingTime = 0;
	private long nextGeoZUpdate;
	private long lastChangeTarget = 0;

	public NpcGameStats(Npc owner) {
		super(owner);
	}

	@Override
	protected void onStatsChange(Effect effect) {
		super.onStatsChange(effect);
		checkSpeedStats();
	}

	@Override
	public StatsTemplate getStatsTemplate() {
		return owner.getObjectTemplate().getStatsTemplate();
	}

	@Override
	public Stat2 getStat(StatEnum statEnum, Stat2 stat, CalculationType... calculationTypes) {
		Stat2 s = super.getStat(statEnum, stat, calculationTypes);
		owner.getAi().modifyOwnerStat(s);
		return s;
	}

	@Override
	public Stat2 getAttackSpeed() {
		return getStat(StatEnum.ATTACK_SPEED, owner.getObjectTemplate().getAttackSpeed());
	}

	@Override
	public Stat2 getMovementSpeed() {
		Stat2 newSpeedStat;
		if (owner.isInState(CreatureState.WEAPON_EQUIPPED)) {
			float speed;
			if (owner.getWalkerGroup() != null)
				speed = getStatsTemplate().getGroupRunSpeedFight();
			else
				speed = getStatsTemplate().getRunSpeedFight();
			newSpeedStat = getStat(StatEnum.SPEED, Math.round(speed * 1000));
		} else if (owner.isInState(CreatureState.WALK_MODE)) {
			float speed;
			if (owner.getWalkerGroup() != null && owner.getAi().getSubState() == AISubState.WALK_PATH)
				speed = getStatsTemplate().getGroupWalkSpeed();
			else
				speed = getStatsTemplate().getWalkSpeed();
			newSpeedStat = getStat(StatEnum.SPEED, Math.round(speed * 1000));
		} else {
			float multiplier = owner.isFlying() ? 1.3f : 1.0f;
			newSpeedStat = getStat(StatEnum.SPEED, Math.round(getStatsTemplate().getRunSpeed() * multiplier * 1000));
		}
		return newSpeedStat;
	}

	@Override
	public Stat2 getAttackRange() {
		return getStat(StatEnum.ATTACK_RANGE, owner.getObjectTemplate().getAttackRange() * 1000);
	}

	@Override
	public Stat2 getHpRegenRate() {
		int divider = 2;
		if (owner.getAbyssNpcType() != AbyssNpcType.NONE)
			divider = 4; // Abyss type related NPCs restore their health by 25%
		return getStat(StatEnum.REGEN_HP, getStatsTemplate().getMaxHp() / divider);
	}

	@Override
	public Stat2 getMpRegenRate() {
		throw new IllegalStateException("No mp regen for NPC");
	}

	public int getCastSpeed() {
		return owner.getObjectTemplate().getCastSpeed();
	}

	public int getLastAttackTimeDelta() {
		return Math.round((System.currentTimeMillis() - lastAttackTime) / 1000f);
	}

	public int getLastAttackedTimeDelta() {
		return Math.round((System.currentTimeMillis() - lastAttackedTime) / 1000f);
	}

	public void renewLastAttackTime() {
		this.lastAttackTime = System.currentTimeMillis();
	}

	public void renewLastAttackedTime() {
		this.lastAttackedTime = System.currentTimeMillis();
	}

	public boolean isNextAttackScheduled() {
		return nextAttackTime - System.currentTimeMillis() > 50;
	}

	public void setFightStartingTime() {
		this.fightStartingTime = System.currentTimeMillis();
	}

	public long getFightStartingTime() {
		return this.fightStartingTime;
	}

	public void setNextAttackTime(long nextAttackTime) {
		this.nextAttackTime = nextAttackTime;
	}

	/**
	 * @return next possible attack time depending on stats
	 */
	public int getNextAttackInterval() {
		long attackDelay = System.currentTimeMillis() - lastAttackTime;
		int attackSpeed = getAttackSpeed().getCurrent();
		if (attackSpeed == 0) {
			attackSpeed = 2000;
		}
		if (owner.getAi().isLogging()) {
			AILogger.info(owner.getAi(), "adelay = " + attackDelay + " aspeed = " + attackSpeed);
		}
		int nextAttack = 0;
		if (lastAttackTime == 0 && !owner.getMoveController().isInMove() && owner.getTarget() instanceof Creature
			&& PositionUtil.isInAttackRange(owner, (Creature) owner.getTarget(), getAttackRange().getCurrent() / 1000f)) {
			nextAttack = 750;
		}
		if (attackDelay < attackSpeed) {
			nextAttack = (int) (attackSpeed - attackDelay);
		}
		return nextAttack;
	}

	public void renewLastSkillTime() {
		this.lastSkillTime = System.currentTimeMillis();
	}

	public void renewLastChangeTargetTime() {
		this.lastChangeTarget = System.currentTimeMillis();
	}

	public int getLastSkillTimeDelta() {
		return Math.round((System.currentTimeMillis() - lastSkillTime) / 1000f);
	}

	public int getLastChangeTargetTimeDelta() {
		return Math.round((System.currentTimeMillis() - lastChangeTarget) / 1000f);
	}

	public long getLastSkillTime() {
		return lastSkillTime;
	}

	public boolean canUseNextSkill() {
		return nextSkillDelay == 0 || System.currentTimeMillis() >= lastSkillTime + nextSkillDelay;
	}

	public void setNextSkillDelay(int nextSkillDelay) {
		if (nextSkillDelay == -1) // xml skills without specific times in templates
			this.nextSkillDelay = Rnd.get(3000, 9000);
		else
			this.nextSkillDelay = nextSkillDelay;
	}

	public void setLastSkill(NpcSkillEntry lastSkill) {
		this.lastSkill = lastSkill;
	}

	public NpcSkillEntry getLastSkill() {
		return lastSkill;
	}

	public final long getNextGeoZUpdate() {
		return nextGeoZUpdate;
	}

	/**
	 * @param nextGeoZUpdate
	 *          the nextGeoZUpdate to set
	 */
	public void setNextGeoZUpdate(long nextGeoZUpdate) {
		this.nextGeoZUpdate = nextGeoZUpdate;
	}

	public void resetFightStats() {
		lastAttackTime = 0;
		lastAttackedTime = 0;
		lastChangeTarget = 0;
		fightStartingTime = 0;
		nextAttackTime = 0;
		lastSkillTime = 0;
		nextSkillDelay = 0;
	}

	/**
	 * @return time until the npc can use a skill for the first time in this fight
	 */
	public int getInitialSkillDelay() {
		return owner.getAi().modifyInitialSkillDelay(Rnd.get(getAttackSpeed().getCurrent(), 3 * getAttackSpeed().getCurrent()));
	}
}
