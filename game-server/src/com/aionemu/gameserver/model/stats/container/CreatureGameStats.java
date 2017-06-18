package com.aionemu.gameserver.model.stats.container;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.RandomBonusEffect;
import com.aionemu.gameserver.model.stats.calc.AdditionStat;
import com.aionemu.gameserver.model.stats.calc.ReverseStat;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatCapUtil;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunctionProxy;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.model.templates.stats.StatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;

/**
 * @author xavier
 * @modified Neon
 */
public abstract class CreatureGameStats<T extends Creature> {

	private static final int ATTACK_MAX_COUNTER = Integer.MAX_VALUE;

	protected final T owner;
	private final Map<StatEnum, TreeSet<IStatFunction>> stats = new EnumMap<>(StatEnum.class);
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private long lastGeoUpdate = 0;
	private int attackCounter = 0;
	private int cachedMaxHp, cachedMaxMp;

	protected CreatureGameStats(T owner) {
		this.owner = owner;
	}

	/**
	 * @return the atcount
	 */
	public int getAttackCounter() {
		return attackCounter;
	}

	/**
	 * @param attackCounter
	 *          the atcount to set
	 */
	protected void setAttackCounter(int attackCounter) {
		if (attackCounter <= 0) {
			this.attackCounter = 1;
		} else {
			this.attackCounter = attackCounter;
		}
	}

	public void increaseAttackCounter() {
		if (attackCounter == ATTACK_MAX_COUNTER) {
			this.attackCounter = 1;
		} else {
			this.attackCounter++;
		}
	}

	public final void addEffectOnly(StatOwner statOwner, List<? extends IStatFunction> functions) {
		lock.writeLock().lock();
		try {
			for (IStatFunction function : functions) {
				TreeSet<IStatFunction> statFunctions = stats.get(function.getName());
				if (statFunctions == null) {
					statFunctions = new TreeSet<>();
					stats.put(function.getName(), statFunctions);
				}
				IStatFunction func = function;
				if (function instanceof StatFunction)
					func = new StatFunctionProxy(statOwner, function);
				statFunctions.add(func);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public final void addEffect(StatOwner statOwner, List<? extends IStatFunction> functions) {
		addEffectOnly(statOwner, functions);
		onStatsChange();
	}

	public final void endEffect(StatOwner statOwner) {
		lock.writeLock().lock();
		try {
			for (Entry<StatEnum, TreeSet<IStatFunction>> e : stats.entrySet()) {
				TreeSet<IStatFunction> value = e.getValue();
				for (Iterator<IStatFunction> iter = value.iterator(); iter.hasNext();) {
					IStatFunction ownedMod = iter.next();
					if (ownedMod.getOwner() != null && ownedMod.getOwner().equals(statOwner)) {
						iter.remove();
					}
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
		onStatsChange();
	}

	public int getPositiveStat(StatEnum statEnum, int base) {
		Stat2 stat = getStat(statEnum, base);
		int value = stat.getCurrent();
		return value > 0 ? value : 0;
	}

	public int getPositiveReverseStat(StatEnum statEnum, int base) {
		Stat2 stat = getReverseStat(statEnum, base);
		int value = stat.getCurrent();
		return value > 0 ? value : 0;
	}

	public Stat2 getStat(StatEnum statEnum, int base) {
		Stat2 stat = new AdditionStat(statEnum, base, owner);
		return getStat(statEnum, stat);
	}

	public Stat2 getStat(StatEnum statEnum, int base, float bonusRate) {
		Stat2 stat = new AdditionStat(statEnum, base, owner, bonusRate);
		return getStat(statEnum, stat);
	}

	public Stat2 getReverseStat(StatEnum statEnum, int base) {
		Stat2 stat = new ReverseStat(statEnum, base, owner);
		return getStat(statEnum, stat);
	}

	public Stat2 getReverseStat(StatEnum statEnum, int base, float bonusRate) {
		Stat2 stat = new ReverseStat(statEnum, base, owner, bonusRate);
		return getStat(statEnum, stat);
	}

	public Stat2 getStat(StatEnum statEnum, Stat2 stat) {
		TreeSet<IStatFunction> functions = getStatsByStatEnum(statEnum);
		if (functions != null) {
			for (IStatFunction func : functions) {
				if (func.validate(stat)) {
					func.apply(stat);
				}
			}
			StatCapUtil.calculateBaseValue(stat, owner.isPlayer());
		}
		return stat;
	}

	public Stat2 getItemStatBoost(StatEnum statEnum, Stat2 stat) {
		TreeSet<IStatFunction> functions = getStatsByStatEnum(statEnum);
		if (functions != null) {
			for (IStatFunction func : functions) {
				if (func.isBonus() && func.validate(stat) && (func.getOwner() instanceof Item || func.getOwner() instanceof ManaStone
					|| func.getOwner() instanceof ItemSetTemplate || func.getOwner() instanceof RandomBonusEffect)) {
					func.apply(stat);
				}
			}
		}
		return stat;
	}

	public abstract StatsTemplate getStatsTemplate();

	public Stat2 getPower() {
		return getStat(StatEnum.POWER, 100);
	}

	public Stat2 getHealth() {
		return getStat(StatEnum.HEALTH, 100);
	}

	public Stat2 getAccuracy() { // FIXME unused
		return getStat(StatEnum.ACCURACY, 100);
	}

	public Stat2 getAgility() { // FIXME unused
		return getStat(StatEnum.AGILITY, 100);
	}

	public Stat2 getKnowledge() {
		return getStat(StatEnum.KNOWLEDGE, 100);
	}

	public Stat2 getWill() {
		return getStat(StatEnum.WILL, 100);
	}

	public Stat2 getMaxHp() {
		return getStat(StatEnum.MAXHP, getStatsTemplate().getMaxHp());
	}

	public Stat2 getMaxMp() {
		return getStat(StatEnum.MAXMP, getStatsTemplate().getMaxMp());
	}

	public Stat2 getPDef() {
		return getStat(StatEnum.PHYSICAL_DEFENSE, getStatsTemplate().getPdef());
	}

	public Stat2 getMDef() {
		return getStat(StatEnum.MAGICAL_DEFEND, getStatsTemplate().getMdef());
	}

	public Stat2 getEvasion() {
		return getStat(StatEnum.EVASION, getStatsTemplate().getEvasion());
	}

	public Stat2 getParry() {
		return getStat(StatEnum.PARRY, getStatsTemplate().getParry());
	}

	public Stat2 getBlock() {
		return getStat(StatEnum.BLOCK, getStatsTemplate().getBlock());
	}

	public Stat2 getMResist() {
		return getStat(StatEnum.MAGICAL_RESIST, getStatsTemplate().getMresist());
	}

	public Stat2 getPCR() {
		return getStat(StatEnum.PHYSICAL_CRITICAL_RESIST, getStatsTemplate().getStrikeResist());
	}

	public Stat2 getMCR() {
		return getStat(StatEnum.MAGICAL_CRITICAL_RESIST, getStatsTemplate().getSpellResist());
	}

	public Stat2 getMainHandPAttack() {
		return getStat(StatEnum.PHYSICAL_ATTACK, getStatsTemplate().getAttack());
	}

	public Stat2 getMainHandPCritical() {
		return getStat(StatEnum.PHYSICAL_CRITICAL, getStatsTemplate().getPcrit());
	}

	public Stat2 getMainHandPAccuracy() {
		return getStat(StatEnum.PHYSICAL_ACCURACY, getStatsTemplate().getAccuracy());
	}

	public Stat2 getMainHandMAttack() {
		return getStat(StatEnum.MAGICAL_ATTACK, getStatsTemplate().getMagicalAttack());
	}

	public Stat2 getMCritical() {
		return getStat(StatEnum.MAGICAL_CRITICAL, getStatsTemplate().getMcrit());
	}

	public Stat2 getMAccuracy() {
		return getStat(StatEnum.MAGICAL_ACCURACY, getStatsTemplate().getMacc());
	}

	public Stat2 getMBoost() {
		return getStat(StatEnum.BOOST_MAGICAL_SKILL, getStatsTemplate().getMagicBoost());
	}

	public Stat2 getMBResist() {
		return getStat(StatEnum.MAGIC_SKILL_BOOST_RESIST, getStatsTemplate().getMsup());
	}

	public abstract Stat2 getAttackSpeed();

	public abstract Stat2 getMovementSpeed();

	public abstract Stat2 getAttackRange();

	public abstract Stat2 getHpRegenRate();

	public abstract Stat2 getMpRegenRate();

	public int getMagicalDefenseFor(SkillElement element) {
		switch (element) {
			case EARTH:
				return getStat(StatEnum.EARTH_RESISTANCE, 0).getCurrent();
			case FIRE:
				return getStat(StatEnum.FIRE_RESISTANCE, 0).getCurrent();
			case WATER:
				return getStat(StatEnum.WATER_RESISTANCE, 0).getCurrent();
			case WIND:
				return getStat(StatEnum.WIND_RESISTANCE, 0).getCurrent();
			case LIGHT:
				return getStat(StatEnum.ELEMENTAL_RESISTANCE_LIGHT, 0).getCurrent();
			case DARK:
				return getStat(StatEnum.ELEMENTAL_RESISTANCE_DARK, 0).getCurrent();
			default:
				return 0;
		}
	}

	public float getMovementSpeedFloat() {
		return getMovementSpeed().getCurrent() / 1000f;
	}

	/**
	 * Send packet about stats info
	 */
	public void updateStatInfo() {
	}

	/**
	 * Send packet about speed info
	 */
	public void updateSpeedInfo() {
	}

	public TreeSet<IStatFunction> getStatsByStatEnum(StatEnum stat) {
		lock.readLock().lock();
		try {
			TreeSet<IStatFunction> allStats = stats.get(stat);
			if (allStats == null)
				return null;
			List<IStatFunction> setFuncs = null;
			for (IStatFunction func : allStats) {
				if (func.getPriority() >= Integer.MAX_VALUE - 10) {
					if (setFuncs == null)
						setFuncs = new ArrayList<>();
					setFuncs.add(func);
				} else if (setFuncs != null) {
					// all StatSetFunctions added
					break;
				}
			}
			if (setFuncs == null)
				return new TreeSet<>(allStats);
			else
				return new TreeSet<>(setFuncs);
		} finally {
			lock.readLock().unlock();
		}
	}

	public boolean checkGeoNeedUpdate() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastGeoUpdate > 600) {
			lastGeoUpdate = currentTime;
			return true;
		}
		return false;
	}

	/**
	 * Perform additional calculations after effects added/removed<br>
	 * This method will be called outside of stats lock.
	 */
	protected void onStatsChange() {
		checkHPStats();
		checkMPStats();
	}

	private void checkHPStats() {
		synchronized (this) {
			int oldMaxHp = cachedMaxHp != 0 ? cachedMaxHp : getMaxHp().getBase();
			int currentMaxHp = cachedMaxHp = getMaxHp().getCurrent();
			if (oldMaxHp != currentMaxHp) {
				float percent = 1f * currentMaxHp / oldMaxHp;
				owner.getLifeStats().setCurrentHp(Math.min(Math.round(owner.getLifeStats().getCurrentHp() * percent), currentMaxHp));
				owner.getLifeStats().onReduceHp(TYPE.REGULAR, 0, 0, LOG.REGULAR);
			}
		}
	}

	private void checkMPStats() {
		synchronized (this) {
			int oldMaxMp = cachedMaxMp != 0 ? cachedMaxMp : getMaxMp().getBase();
			int currentMaxMp = cachedMaxMp = getMaxMp().getCurrent();
			if (oldMaxMp != currentMaxMp) {
				float percent = 1f * currentMaxMp / oldMaxMp;
				owner.getLifeStats().setCurrentMp(Math.min(Math.round(owner.getLifeStats().getCurrentMp() * percent), currentMaxMp));
				owner.getLifeStats().onReduceMp(TYPE.MP, 0, 0, LOG.REGULAR);
			}
		}
	}
}
