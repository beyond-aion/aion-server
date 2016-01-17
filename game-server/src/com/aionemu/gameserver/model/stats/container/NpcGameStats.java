package com.aionemu.gameserver.model.stats.container;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Logger;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.stats.NpcStatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

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
	public Stat2 getMaxHp() {
		Stat2 stat = getStat(StatEnum.MAXHP, owner.getObjectTemplate().getStatsTemplate().getMaxHp());
		if (owner.getSpawn() instanceof SiegeSpawnTemplate
			&& owner.getRating() == NpcRating.LEGENDARY
			&& (owner.getObjectTemplate().getAbyssNpcType() == AbyssNpcType.BOSS || owner.getObjectTemplate().getAbyssNpcType() == AbyssNpcType.GUARD
				|| owner.getRace() == Race.GHENCHMAN_LIGHT || owner.getRace() == Race.GHENCHMAN_DARK))
			stat.setBaseRate(SiegeConfig.SIEGE_HEALTH_MULTIPLIER);
		return stat;
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
		int value = owner.getObjectTemplate().getStatsTemplate().getStrikeResist();
		if (value == 0)
			value = getStrikeResist();
		return getStat(StatEnum.PHYSICAL_CRITICAL_RESIST, value);
	}

	@Override
	public Stat2 getMCR() {
		return getStat(StatEnum.MAGICAL_CRITICAL_RESIST, owner.getObjectTemplate().getStatsTemplate().getSpellResist());
	}

	@Override
	public Stat2 getAllSpeed() {
		return getStat(StatEnum.ALLSPEED, 7500); // TODO current value
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
		int level = owner.getLevel();
		if (owner.getRating() == NpcRating.NORMAL) { // FIXME: fix templates or formula
			if (level < 25)
				mres *= 0.7f;
			else if (level >= 25 && level < 50)
				mres *= 0.8f;
			else
				mres *= 0.9f;
		}
		return getStat(StatEnum.MAGICAL_RESIST, mres);
	}

	@Override
	public Stat2 getMBResist() {
		int msup = owner.getObjectTemplate().getStatsTemplate().getMsup();
		if (msup == 0)
			msup = getMsup();
		return getStat(StatEnum.MAGIC_SKILL_BOOST_RESIST, msup);
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
		int value = owner.getObjectTemplate().getStatsTemplate().getParry();
		if (value == 0)
			value = getParryAmount();
		return getStat(StatEnum.PARRY, value);
	}

	@Override
	public Stat2 getBlock() {
		return getStat(StatEnum.BLOCK, owner.getObjectTemplate().getStatsTemplate().getBlock());
	}

	@Override
	public Stat2 getMainHandPAttack() {
		int atk = owner.getObjectTemplate().getStatsTemplate().getAttack();
		if (owner.getRating() == NpcRating.NORMAL) { // FIXME: fix templates or formula
			if (owner.getLevel() <= 50)
				atk *= 0.75f;
			else
				atk *= 0.8f;
		}
		return getStat(StatEnum.PHYSICAL_ATTACK, atk);
	}

	@Override
	public Stat2 getMainHandPCritical() {
		return getStat(StatEnum.PHYSICAL_CRITICAL, owner.getObjectTemplate().getStatsTemplate().getPcrit());
	}

	@Override
	public Stat2 getMainHandPAccuracy() {
		return getStat(StatEnum.PHYSICAL_ACCURACY, getMainHandAccuracy()); // FIXME: Recalculate template values
		// return getStat(StatEnum.PHYSICAL_ACCURACY, owner.getObjectTemplate().getStatsTemplate().getMainHandAccuracy());
	}

	@Override
	public Stat2 getMainHandMAttack() {
		int matk = owner.getObjectTemplate().getStatsTemplate().getMagicalAttack();
		return getStat(StatEnum.MAGICAL_ATTACK, owner.getAi2().modifyMattack(matk));
	}

	@Override
	public Stat2 getMBoost() {
		return getStat(StatEnum.BOOST_MAGICAL_SKILL, owner.getObjectTemplate().getStatsTemplate().getMagicBoost());
	}

	@Override
	public Stat2 getMAccuracy() {
		int base = owner.getAi2().modifyMaccuracy(Math.round(owner.getObjectTemplate().getStatsTemplate().getMacc() * 0.8f));
		return getStat(StatEnum.MAGICAL_ACCURACY, base);
	}

	@Override
	public Stat2 getMCritical() {
		int value = owner.getObjectTemplate().getStatsTemplate().getMcrit();
		if (value == 0)
			value = 50;
		return getStat(StatEnum.MAGICAL_CRITICAL, value);
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

	// TODO: Remove this after transfering into npc_templates
	private int getMainHandAccuracy() {
		return Math.round(owner.getLevel() * 40 * getRatingModifier(Stat.MAIN_HAND_ACCURACY) * getRankModifier(Stat.MAIN_HAND_ACCURACY));
	}

	private int getParryAmount() {
		return Math.round(owner.getLevel() * 40 * getRatingModifier(Stat.PARRY) * getRankModifier(Stat.PARRY));
	}

	private int getMsup() {
		if (owner.getLevel() < 60)
			return 0;

		return Math.round(owner.getLevel() * 3 * getRatingModifier(Stat.MSUP) * getRankModifier(Stat.MSUP));
	}

	private int getStrikeResist() {
		if (owner.getLevel() < 60)
			return 0;

		return Math.round(owner.getLevel() * 2.2f * getRatingModifier(Stat.STRIKE_RESIST) * getRankModifier(Stat.STRIKE_RESIST));
	}

	private float getRankModifier(Stat stat) {
		switch (owner.getRank()) {
			case NOVICE:
				switch (stat) {
					case PARRY:
					case MAIN_HAND_ACCURACY:
					case MSUP:
					case STRIKE_RESIST:
						return 1.0f;
				}
				break;
			case DISCIPLINED:
				switch (stat) {
					case PARRY:
						return 1.05f;
					case MAIN_HAND_ACCURACY:
						return 1.01f;
					case MSUP:
					case STRIKE_RESIST:
						return 1.5f;
				}
				break;
			case SEASONED:
				switch (stat) {
					case PARRY:
						return 1.1f;
					case MAIN_HAND_ACCURACY:
						return 1.02f;
					case MSUP:
					case STRIKE_RESIST:
						return 2.0f;
				}
				break;
			case EXPERT:
				switch (stat) {
					case PARRY:
						return 1.1f;
					case MAIN_HAND_ACCURACY:
						return 1.03f;
					case MSUP:
						return 2.5f;
					case STRIKE_RESIST:
						return 2.4f;
				}
				break;
			case VETERAN:
				switch (stat) {
					case PARRY:
						return 1.12f;
					case MAIN_HAND_ACCURACY:
						return 1.04f;
					case MSUP:
						return 3.0f;
					case STRIKE_RESIST:
						return 2.5f;
				}
				break;
			case MASTER:
				switch (stat) {
					case PARRY:
						return 1.12f;
					case MAIN_HAND_ACCURACY:
						return 1.05f;
					case MSUP:
						return 3.0f;
					case STRIKE_RESIST:
						return 2.6f;
				}
				break;
		}
		return 0;
	}

	private float getRatingModifier(Stat stat) {
		switch (owner.getRating()) {
			case JUNK:
				switch (stat) {
					case PARRY:
					case MAIN_HAND_ACCURACY:
						return 1.0f;
					case MSUP:
					case STRIKE_RESIST:
						return 0;
				}
				break;
			case NORMAL:
				switch (stat) {
					case MAIN_HAND_ACCURACY:
						return 1.025f;
					case PARRY:
					case MSUP:
					case STRIKE_RESIST:
						return 1.0f;
				}
				break;
			case ELITE:
				switch (stat) {
					case PARRY:
						return 1.025f;
					case MAIN_HAND_ACCURACY:
						return 1.05f;
					case MSUP:
						return 1.5f;
					case STRIKE_RESIST:
						return 1.5f;
				}
				break;
			case HERO:
				switch (stat) {
					case PARRY:
						return 1.08f;
					case MAIN_HAND_ACCURACY:
						return 1.075f;
					case MSUP:
						return 2.0f;
					case STRIKE_RESIST:
						return 1.75f;
				}
				break;
			case LEGENDARY:
				switch (stat) {
					case PARRY:
						return 1.1f;
					case MAIN_HAND_ACCURACY:
						return 1.1f;
					case MSUP:
						return 2.5f;
					case STRIKE_RESIST:
						return 2.1f;
				}
				break;
		}
		return 0;
	}
}

enum Stat {
	PARRY,
	MAIN_HAND_ACCURACY,
	MSUP,
	STRIKE_RESIST;
}
