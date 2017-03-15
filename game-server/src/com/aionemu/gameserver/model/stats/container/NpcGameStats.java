package com.aionemu.gameserver.model.stats.container;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.stats.StatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author xavier
 * @modified Estrayl
 */
public class NpcGameStats extends CreatureGameStats<Npc> {

	int currentRunSpeed = 0;
	private long lastAttackTime = 0;
	private long lastAttackedTime = 0;
	private long nextAttackTime = 0;
	private long lastSkillTime = 0;
	private long nextSkillTime = 0;
	private NpcSkillEntry lastSkill = null;
	private long fightStartingTime = 0;
	private int cachedState;
	private AISubState cachedSubState;
	private Stat2 cachedSpeedStat;
	private long lastGeoZUpdate;
	private long lastChangeTarget = 0;

	public NpcGameStats(Npc owner) {
		super(owner);
	}

	@Override
	protected void onStatsChange() {
		super.onStatsChange();
		checkSpeedStats();
	}

	private void checkSpeedStats() {
		Stat2 oldSpeed = cachedSpeedStat;
		cachedSpeedStat = null;
		Stat2 newSpeed = getMovementSpeed();
		cachedSpeedStat = newSpeed;
		if (oldSpeed == null || oldSpeed.getCurrent() != newSpeed.getCurrent()) {
			updateSpeedInfo();
		}
	}

	@Override
	public StatsTemplate getStatsTemplate() {
		return owner.getObjectTemplate().getStatsTemplate();
	}

	@Override
	public Stat2 getStat(StatEnum statEnum, Stat2 stat) {
		Stat2 s = super.getStat(statEnum, stat);
		owner.getAi().modifyOwnerStat(s);
		return s;
	}

	@Override
	public Stat2 getMaxHp() {
		Stat2 stat = super.getMaxHp();
		if (owner.getSpawn() instanceof SiegeSpawnTemplate
			&& owner.getRating() == NpcRating.LEGENDARY
			&& (owner.getObjectTemplate().getAbyssNpcType() == AbyssNpcType.BOSS || owner.getObjectTemplate().getAbyssNpcType() == AbyssNpcType.GUARD
				|| owner.getRace() == Race.GHENCHMAN_LIGHT || owner.getRace() == Race.GHENCHMAN_DARK))
			stat.setBaseRate(SiegeConfig.SIEGE_HEALTH_MULTIPLIER);
		return stat;
	}

	@Override
	public Stat2 getAttackSpeed() {
		return getStat(StatEnum.ATTACK_SPEED, owner.getObjectTemplate().getAttackDelay());
	}

	@Override
	public Stat2 getMovementSpeed() {
		int currentState = owner.getState();
		AISubState currentSubState = owner.getAi().getSubState();
		if (cachedSpeedStat != null && cachedState == currentState && cachedSubState == currentSubState) {
			return cachedSpeedStat;
		}
		Stat2 newSpeedStat = null;
		if (owner.isInState(CreatureState.WEAPON_EQUIPPED)) {
			float speed = 0;
			if (owner.getWalkerGroup() != null)
				speed = getStatsTemplate().getGroupRunSpeedFight();
			else
				speed = getStatsTemplate().getRunSpeedFight();
			newSpeedStat = getStat(StatEnum.SPEED, Math.round(speed * 1000));
		} else if (owner.isInState(CreatureState.WALK_MODE)) {
			float speed = 0;
			if (owner.getWalkerGroup() != null && owner.getAi().getSubState() == AISubState.WALK_PATH)
				speed = getStatsTemplate().getGroupWalkSpeed();
			else
				speed = getStatsTemplate().getWalkSpeed();
			newSpeedStat = getStat(StatEnum.SPEED, Math.round(speed * 1000));
		} else {
			float multiplier = owner.isFlying() ? 1.3f : 1.0f;
			newSpeedStat = getStat(StatEnum.SPEED, Math.round(getStatsTemplate().getRunSpeed() * multiplier * 1000));
		}
		cachedState = currentState;
		cachedSubState = currentSubState;
		cachedSpeedStat = newSpeedStat;
		return newSpeedStat;
	}

	@Override
	public Stat2 getAttackRange() {
		return getStat(StatEnum.ATTACK_RANGE, owner.getObjectTemplate().getAttackRange() * 1000);
	}

	@Override
	public Stat2 getHpRegenRate() {
		return getStat(StatEnum.REGEN_HP, getStatsTemplate().getMaxHp() / 4);
	}

	@Override
	public Stat2 getMpRegenRate() {
		throw new IllegalStateException("No mp regen for NPC");
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
		if (lastAttackTime == 0 && owner.getTarget() instanceof Creature
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

	// not used at the moment
	/*
	 * public void renewLastSkilledTime() { this.lastSkilledTime = System.currentTimeMillis(); }
	 */

	public void renewLastChangeTargetTime() {
		this.lastChangeTarget = System.currentTimeMillis();
	}

	public int getLastSkillTimeDelta() {
		return Math.round((System.currentTimeMillis() - lastSkillTime) / 1000f);
	}

	// not used at the moment
	/*
	 * public int getLastSkilledTimeDelta() { return Math.round((System.currentTimeMillis() - lastSkilledTime) / 1000f); }
	 */

	public int getLastChangeTargetTimeDelta() {
		return Math.round((System.currentTimeMillis() - lastChangeTarget) / 1000f);
	}
	
	public long getLastSkillTime() {
		return lastSkillTime;
	}

	// only use skills after a minimum cooldown of 3 to 9 seconds
	// TODO: Check wether this is a suitable time or not
	public boolean canUseNextSkill() {
		if (nextSkillTime < 0) {
			if (getLastSkillTimeDelta() >= 6 + Rnd.get(-3, 3))
				return true;
		} else {
			if (System.currentTimeMillis() >= lastSkillTime + nextSkillTime)
				return true;
		}
		return false;
	}

	public void setNextSkillTime(int nextSkillTime) {
		this.nextSkillTime = nextSkillTime;
	}

	public void setLastSkill(NpcSkillEntry lastSkill) {
		this.lastSkill = lastSkill;
	}
	
	public NpcSkillEntry getLastSkill() {
		return lastSkill;
	}
	
	@Override
	public void updateSpeedInfo() {
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
	}

	public final long getLastGeoZUpdate() {
		return lastGeoZUpdate;
	}

	/**
	 * @param lastGeoZUpdate
	 *          the lastGeoZUpdate to set
	 */
	public void setLastGeoZUpdate(long lastGeoZUpdate) {
		this.lastGeoZUpdate = lastGeoZUpdate;
	}

	public void resetFightStats() {
		lastAttackTime = 0;
		lastAttackedTime = 0;
		lastChangeTarget = 0;
		fightStartingTime = 0;
		nextAttackTime = 0;
		lastSkillTime = 0;
		nextSkillTime = 0;
	}
}
