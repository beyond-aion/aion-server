package com.aionemu.gameserver.model.stats.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.enchants.EnchantEffect;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.RandomBonusEffect;
import com.aionemu.gameserver.model.stats.calc.*;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunctionProxy;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.model.templates.stats.StatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author xavier, Neon
 */
public abstract class CreatureGameStats<T extends Creature> {

	private static final int ATTACK_MAX_COUNTER = Integer.MAX_VALUE;

	protected final T owner;
	private final Map<StatEnum, List<IStatFunction>> stats = new ConcurrentHashMap<>();

	private int attackCounter = 0;
	private int cachedMaxHp, cachedMaxMp, cachedSpeed;

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

	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public final void addEffectOnly(StatOwner statOwner, List<? extends IStatFunction> functions) {
		for (IStatFunction function : functions) {
			IStatFunction functionToAdd = Objects.equals(statOwner, function.getOwner()) ? function : new StatFunctionProxy(statOwner, function);
			stats.compute(functionToAdd.getName(), (k, statFunctions) -> {
				if (statFunctions == null) {
					statFunctions = new ArrayList<>();
					statFunctions.add(functionToAdd);
				} else {
					synchronized (statFunctions) {
						statFunctions.add(functionToAdd);
						statFunctions.sort(null);
					}
				}
				return statFunctions;
			});
		}
	}

	public final void addEffect(StatOwner statOwner, List<? extends IStatFunction> functions) {
		addEffectOnly(statOwner, functions);
		onStatsChange(statOwner instanceof Effect effect ? effect : null);
	}

	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public final void endEffect(StatOwner statOwner) {
		boolean statsChanged = false;
		for (List<IStatFunction> functions : stats.values()) {
			synchronized (functions) {
					statsChanged |= functions.removeIf(statFunction -> statOwner.equals(statFunction.getOwner()));
			}
		}
		if (statsChanged && !owner.isDead())
			onStatsChange(null);
	}

	public float getPositiveStat(StatEnum statEnum, float base) {
		Stat2 stat = getStat(statEnum, base);
		float value = stat.getCurrent();
		return value > 0 ? value : 0;
	}

	public int getPositiveReverseStat(StatEnum statEnum, int base) {
		Stat2 stat = getReverseStat(statEnum, base);
		int value = stat.getCurrent();
		return value > 0 ? value : 0;
	}

	public Stat2 getStat(StatEnum statEnum, float base, CalculationType... calculationTypes) {
		Stat2 stat = new AdditionStat(statEnum, base, owner);
		return getStat(statEnum, stat, calculationTypes);
	}

	public Stat2 getStat(StatEnum statEnum, float base, float bonusRate, CalculationType... calculationTypes) {
		Stat2 stat = new AdditionStat(statEnum, base, owner, bonusRate);
		return getStat(statEnum, stat, calculationTypes);
	}

	public Stat2 getReverseStat(StatEnum statEnum, float base) {
		Stat2 stat = new ReverseStat(statEnum, base, owner);
		return getStat(statEnum, stat);
	}

	public Stat2 getReverseStat(StatEnum statEnum, float base, float bonusRate) {
		Stat2 stat = new ReverseStat(statEnum, base, owner, bonusRate);
		return getStat(statEnum, stat);
	}

	public Stat2 getStat(StatEnum statEnum, Stat2 stat, CalculationType... calculationTypes) {
		List<IStatFunction> functions = getStatsSorted(statEnum);
		if (functions != null) {
			for (IStatFunction func : functions) {
				if (func.validate(stat)) {
					if ((statEnum == StatEnum.PHYSICAL_ATTACK || statEnum == StatEnum.MAGICAL_ATTACK) && func.getOwner() instanceof EnchantEffect ef) {
						if (ef.getItemSlot() == ItemSlot.MAIN_HAND && ArrayUtils.contains(calculationTypes, CalculationType.MAIN_HAND)
								|| ef.getItemSlot() == ItemSlot.SUB_HAND && ArrayUtils.contains(calculationTypes, CalculationType.OFF_HAND)) {
							func.apply(stat, calculationTypes);
						}
					} else {
						func.apply(stat, calculationTypes);
					}
				}
			}
			StatCapUtil.calculateBaseValue(stat, owner);
		}
		return stat;
	}

	public Stat2 getItemStatBoost(StatEnum statEnum, Stat2 stat) {
		List<IStatFunction> functions = getStatsSorted(statEnum);
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
		return getStat(StatEnum.POWER, getStatsTemplate().getPower());
	}

	public Stat2 getHealth() {
		return getStat(StatEnum.HEALTH, getStatsTemplate().getHealth());
	}

	public Stat2 getAccuracy() {
		return getStat(StatEnum.ACCURACY, getStatsTemplate().getBaseAccuracy());
	}

	public Stat2 getAgility() {
		return getStat(StatEnum.AGILITY, getStatsTemplate().getAgility());
	}

	public Stat2 getKnowledge() {
		return getStat(StatEnum.KNOWLEDGE, getStatsTemplate().getKnowledge());
	}

	public Stat2 getWill() {
		return getStat(StatEnum.WILL, getStatsTemplate().getWill());
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

	public Stat2 getMainHandPAttack(CalculationType... calculationTypes) {
		return getStat(StatEnum.PHYSICAL_ATTACK, getStatsTemplate().getAttack(), calculationTypes);
	}

	public Stat2 getMainHandPCritical() {
		return getStat(StatEnum.PHYSICAL_CRITICAL, getStatsTemplate().getPcrit());
	}

	public Stat2 getMainHandPAccuracy() {
		return getStat(StatEnum.PHYSICAL_ACCURACY, getStatsTemplate().getAccuracy());
	}

	public Stat2 getMainHandMAttack(CalculationType... calculationTypes) {
		return getStat(StatEnum.MAGICAL_ATTACK, getStatsTemplate().getMagicalAttack(), calculationTypes);
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

	public Stat2 getAbnormalResistance() {
		return getStat(StatEnum.ABNORMAL_RESISTANCE_ALL, getStatsTemplate().getAbnormalResistance());
	}

	public Stat2 getResistance(StatEnum statEnum) {
		int base = switch (statEnum) {
			case OPENAERIAL_RESISTANCE, PARALYZE_RESISTANCE, SPIN_RESISTANCE, STAGGER_RESISTANCE, STUMBLE_RESISTANCE, STUN_RESISTANCE -> getStatsTemplate().getStunLikeResistance();
			default -> 0;
		};
		return getStat(statEnum, base);
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
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.CHANGE_SPEED));
	}

	protected boolean checkSpeedStats() {
		int currentSpeed = getMovementSpeed().getCurrent();
		if (currentSpeed != cachedSpeed) {
			updateSpeedInfo();
			cachedSpeed = currentSpeed;
			return true;
		}
		return false;
	}

	/**
	 * @return All stat functions for the given stat sorted by priority
	 */
	public List<IStatFunction> getStatsSorted(StatEnum stat) {
		List<IStatFunction> statFunctions = stats.get(stat);
		if (statFunctions == null)
			return null;
		synchronized (statFunctions) {
				return new ArrayList<>(statFunctions);
		}
	}

	/**
	 * Perform additional calculations after effects added/removed<br>
	 * This method will be called outside of stats lock.
	 */
	protected void onStatsChange(Effect effect) {
		checkMaxHPChanged(effect);
		checkMaxMPChanged(effect);
	}

	private void checkMaxHPChanged(Effect effect) {
		synchronized (this) {
			int oldMaxHp = cachedMaxHp != 0 ? cachedMaxHp : getMaxHp().getBase();
			int currentMaxHp = cachedMaxHp = getMaxHp().getCurrent();
			if (oldMaxHp != currentMaxHp) {
				float percent = 1f * currentMaxHp / oldMaxHp;
				int newHp = Math.min(Math.round(owner.getLifeStats().getCurrentHp() * percent), currentMaxHp);
				Creature effector = effect == null ? owner : effect.getEffector();
				owner.getLifeStats().setCurrentHp(newHp, effector);
			}
		}
	}

	private void checkMaxMPChanged(Effect effect) {
		synchronized (this) {
			int oldMaxMp = cachedMaxMp != 0 ? cachedMaxMp : getMaxMp().getBase();
			int currentMaxMp = cachedMaxMp = getMaxMp().getCurrent();
			if (oldMaxMp != currentMaxMp) {
				float percent = 1f * currentMaxMp / oldMaxMp;
				owner.getLifeStats().setCurrentMp(Math.min(Math.round(owner.getLifeStats().getCurrentMp() * percent), currentMaxMp));
			}
		}
	}
}
