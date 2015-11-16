package com.aionemu.gameserver.model.stats.container;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Logger;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.templates.stats.NpcStatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xavier
 */
public class NpcGameStats extends CreatureGameStats<Npc> {

	int currentRunSpeed = 0;
	private long lastAttackTime = 0;
	private long lastAttackedTime = 0;
	private long nextAttackTime = 0;
	private long lastSkillTime = 0;
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
	public Stat2 getMaxHp() {
		return getStat(StatEnum.MAXHP, owner.getObjectTemplate().getStatsTemplate().getMaxHp());
	}

	@Override
	public Stat2 getMaxMp() {
		return getStat(StatEnum.MAXMP, owner.getObjectTemplate().getStatsTemplate().getMaxMp());
	}

	@Override
	public Stat2 getAttackSpeed() {
		return getStat(StatEnum.ATTACK_SPEED, owner.getObjectTemplate().getAttackDelay());
	}

	@Override
	public Stat2 getPCR() {
		return getStat(StatEnum.PHYSICAL_CRITICAL_RESIST, 0);
	}

	@Override
	public Stat2 getMCR() {
		return getStat(StatEnum.MAGICAL_CRITICAL_RESIST, 0);
	}

	@Override
	public Stat2 getAllSpeed() {
		int base = 7500; // TODO current value
		return getStat(StatEnum.ALLSPEED, base);
	}

	@Override
	public Stat2 getMovementSpeed() {
		int currentState = owner.getState();
		AISubState currentSubState = owner.getAi2().getSubState();
		Stat2 cachedSpeed = cachedSpeedStat;
		if (cachedSpeed != null && cachedState == currentState && cachedSubState == currentSubState) {
			return cachedSpeed;
		}
		Stat2 newSpeedStat = null;
		if (owner.isInState(CreatureState.WEAPON_EQUIPPED)) {
			float speed = 0;
			if (owner.getWalkerGroup() != null)
				speed = owner.getObjectTemplate().getStatsTemplate().getGroupRunSpeedFight();
			else
				speed = owner.getObjectTemplate().getStatsTemplate().getRunSpeedFight();
			newSpeedStat = getStat(StatEnum.SPEED, Math.round(speed * 1000));
		} else if (owner.isInState(CreatureState.WALKING)) {
			float speed = 0;
			if (owner.getWalkerGroup() != null && owner.getAi2().getSubState() == AISubState.WALK_PATH)
				speed = owner.getObjectTemplate().getStatsTemplate().getGroupWalkSpeed();
			else
				speed = owner.getObjectTemplate().getStatsTemplate().getWalkSpeed();
			newSpeedStat = getStat(StatEnum.SPEED, Math.round(speed * 1000));
		} else {
			float multiplier = owner.isFlying() ? 1.3f : 1.0f;
			newSpeedStat = getStat(StatEnum.SPEED, Math.round(owner.getObjectTemplate().getStatsTemplate().getRunSpeed() * multiplier * 1000));
		}
		cachedState = currentState;
		cachedSpeedStat = newSpeedStat;
		return newSpeedStat;
	}

	@Override
	public Stat2 getAttackRange() {
		return getStat(StatEnum.ATTACK_RANGE, owner.getObjectTemplate().getAttackRange() * 1000);
	}

	@Override
	public Stat2 getPDef() {
		int pdef = owner.getObjectTemplate().getStatsTemplate().getPdef();
		return getStat(StatEnum.PHYSICAL_DEFENSE, owner.getAi2().modifyPdef(pdef));
	}

	@Override
	public Stat2 getMDef() {
		return getStat(StatEnum.MAGICAL_DEFEND, 0);
	}

	@Override
	public Stat2 getMResist() {
		int mres = owner.getObjectTemplate().getStatsTemplate().getMresist();
		if (owner.getLevel() < 50)
			mres *= 0.8f;
		return getStat(StatEnum.MAGICAL_RESIST, mres);
	}

	@Override
	public Stat2 getMBResist() {
		return getStat(StatEnum.MAGIC_SKILL_BOOST_RESIST, 0);
	}

	@Override
	public Stat2 getPower() {
		return getStat(StatEnum.POWER, 100);
	}

	@Override
	public Stat2 getHealth() {
		return getStat(StatEnum.HEALTH, 100);
	}

	@Override
	public Stat2 getAccuracy() {
		return getStat(StatEnum.ACCURACY, 100);
	}

	@Override
	public Stat2 getAgility() {
		return getStat(StatEnum.AGILITY, 100);
	}

	@Override
	public Stat2 getKnowledge() {
		return getStat(StatEnum.KNOWLEDGE, 100);
	}

	@Override
	public Stat2 getWill() {
		return getStat(StatEnum.WILL, 100);
	}

	@Override
	public Stat2 getEvasion() {
		return getStat(StatEnum.EVASION, owner.getObjectTemplate().getStatsTemplate().getEvasion());
	}

	@Override
	public Stat2 getParry() {
		return getStat(StatEnum.PARRY, 100);
	}

	@Override
	public Stat2 getBlock() {
		return getStat(StatEnum.BLOCK, 0);
	}

	@Override
	public Stat2 getMainHandPAttack() {
		return getStat(StatEnum.PHYSICAL_ATTACK, owner.getObjectTemplate().getStatsTemplate().getMainHandAttack());
	}

	@Override
	public Stat2 getMainHandPCritical() {
		return getStat(StatEnum.PHYSICAL_CRITICAL, 10);
	}

	@Override
	public Stat2 getMainHandPAccuracy() {
		return getStat(StatEnum.PHYSICAL_ACCURACY, owner.getObjectTemplate().getStatsTemplate().getMainHandAccuracy());
	}

	@Override
	public Stat2 getMainHandMAttack() {
		int power = owner.getObjectTemplate().getStatsTemplate().getPower();
		return getStat(StatEnum.MAGICAL_ATTACK, Math.round(owner.getAi2().modifyMattack(power)));
	}

	@Override
	public Stat2 getMBoost() {
		return getStat(StatEnum.BOOST_MAGICAL_SKILL, 100);
	}

	@Override
	public Stat2 getMAccuracy() {
		int base = owner.getAi2().modifyMaccuracy(Math.round(owner.getObjectTemplate().getStatsTemplate().getAccuracy()));
		return getStat(StatEnum.MAGICAL_ACCURACY, base);
	}

	@Override
	public Stat2 getMCritical() {
		return getStat(StatEnum.MAGICAL_CRITICAL, 50);
	}

	@Override
	public Stat2 getHpRegenRate() {
		NpcStatsTemplate nst = owner.getObjectTemplate().getStatsTemplate();
		return getStat(StatEnum.REGEN_HP, nst.getMaxHp() / 4);
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
		if (owner.getAi2().isLogging()) {
			AI2Logger.info(owner.getAi2(), "adelay = " + attackDelay + " aspeed = " + attackSpeed);
		}
		int nextAttack = 0;
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

	// only use skills after a minimum cooldown of 3 to 9 seconds
	// TODO: Check wether this is a suitable time or not
	public boolean canUseNextSkill() {
		if (getLastSkillTimeDelta() >= 6 + Rnd.get(-3, 3))
			return true;
		else
			return false;
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

}
