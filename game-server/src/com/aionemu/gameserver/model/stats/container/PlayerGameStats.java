package com.aionemu.gameserver.model.stats.container;

import org.apache.commons.lang3.ArrayUtils;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.stats.calc.AdditionStat;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.model.templates.ride.RideInfo;
import com.aionemu.gameserver.model.templates.stats.StatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author xavier
 */
public class PlayerGameStats extends CreatureGameStats<Player> {

	private StatsTemplate statsTemplate;
	private int cachedAttackSpeed;
	private int maxDamageChance;
	private float minDamageRatio;
	private float skillEfficiency;

	public PlayerGameStats(Player owner) {
		super(owner);
		updateStatsTemplate();
	}

	@Override
	protected void onStatsChange(Effect effect) {
		super.onStatsChange(effect);
		updateStatsVisually();
		checkSpeedStats();
	}

	public void updateStatsAndSpeedVisually() {
		onStatsChange(null);
	}

	public void updateStatsVisually() {
		updateStatInfo();
	}

	@Override
	protected boolean checkSpeedStats() {
		boolean speedChanged = super.checkSpeedStats();
		int currentAttackSpeed = getAttackSpeed().getCurrent();
		if (currentAttackSpeed != cachedAttackSpeed) {
			if (!speedChanged) // prevent double packet broadcast (super.checkSpeedStats() already broadcasts on true)
				updateSpeedInfo();
			cachedAttackSpeed = currentAttackSpeed;
			return true;
		}
		return speedChanged;
	}

	@Override
	public StatsTemplate getStatsTemplate() {
		return statsTemplate;
	}

	public void updateStatsTemplate() {
		this.statsTemplate = owner.getPlayerClass().createStatsTemplate(owner.getLevel());
	}

	public Stat2 getMaxDp() {
		return getStat(StatEnum.MAXDP, 4000);
	}

	public Stat2 getFlyTime() {
		return getStat(StatEnum.FLY_TIME, CustomConfig.BASE_FLYTIME);
	}

	@Override
	public Stat2 getAttackSpeed() {
		int base = 1500;
		Equipment equipment = owner.getEquipment();
		Item mainHandWeapon = equipment.getMainHandWeapon();

		if (mainHandWeapon != null) {
			base = mainHandWeapon.getItemTemplate().getWeaponStats().getAttackSpeed();
			Item offWeapon = owner.getEquipment().getOffHandWeapon();
			if (offWeapon == mainHandWeapon)
				offWeapon = null;
			if (offWeapon != null)
				base += offWeapon.getItemTemplate().getWeaponStats().getAttackSpeed() / 4;
		}
		return getStat(StatEnum.ATTACK_SPEED, base);
	}

	@Override
	public Stat2 getMovementSpeed() {
		Stat2 movementSpeed;
		StatsTemplate pst = getStatsTemplate();
		if (owner.isInPlayerMode(PlayerMode.RIDE)) {
			RideInfo ride = owner.ride;
			int runSpeed = (int) pst.getRunSpeed() * 1000;
			if (owner.isInState(CreatureState.FLYING)) {
				movementSpeed = new AdditionStat(StatEnum.FLY_SPEED, runSpeed, owner);
				movementSpeed.addToBonus((int) (ride.getFlySpeed() * 1000) - runSpeed);
			} else {
				float speed = owner.isInSprintMode() ? ride.getSprintSpeed() : ride.getMoveSpeed();
				movementSpeed = new AdditionStat(StatEnum.SPEED, runSpeed, owner);
				movementSpeed.addToBonus((int) (speed * 1000) - runSpeed);
			}
		} else if (owner.isInFlyingState())
			movementSpeed = getStat(StatEnum.FLY_SPEED, Math.round(pst.getFlySpeed() * 1000));
		else if (owner.isInState(CreatureState.FLYING) && !owner.isInState(CreatureState.RESTING))
			movementSpeed = getStat(StatEnum.SPEED, 12000);
		else if (owner.isInState(CreatureState.WALK_MODE))
			movementSpeed = getStat(StatEnum.SPEED, Math.round(pst.getWalkSpeed() * 1000));
		else
			movementSpeed = getStat(StatEnum.SPEED, Math.round(pst.getRunSpeed() * 1000));
		return movementSpeed;
	}

	@Override
	public Stat2 getAttackRange() {
		int base = 1500;
		int minWeaponRange = Integer.MAX_VALUE;
		Equipment equipment = owner.getEquipment();
		Item mainHandWeapon = equipment.getMainHandWeapon();
		Item offHandWeapon = equipment.getOffHandWeapon();
		if (mainHandWeapon != null)
			minWeaponRange = mainHandWeapon.getItemTemplate().getWeaponStats().getAttackRange();
		if (offHandWeapon != null && !equipment.isShieldEquipped())
			minWeaponRange = Math.min(minWeaponRange, offHandWeapon.getItemTemplate().getWeaponStats().getAttackRange());
		return getStat(StatEnum.ATTACK_RANGE, minWeaponRange == Integer.MAX_VALUE ? base : minWeaponRange);
	}

	@Override
	public Stat2 getParry() {
		int base = getStatsTemplate().getParry();
		Item mainHandWeapon = owner.getEquipment().getMainHandWeapon();
		if (mainHandWeapon != null) {
			base += mainHandWeapon.getItemTemplate().getWeaponStats().getParry();
		}
		return getStat(StatEnum.PARRY, base);
	}

	@Override
	public Stat2 getMainHandPAttack(CalculationType... calculationTypes) {
		calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.MAIN_HAND);
		float base = getStatsTemplate().getAttack();
		Equipment equipment = owner.getEquipment();
		Item mainHandWeapon = equipment.getMainHandWeapon();
		if (mainHandWeapon != null) {
			if (mainHandWeapon.getItemTemplate().getAttackType().isMagical())
				return new AdditionStat(StatEnum.MAIN_HAND_POWER, 0, owner);
			if (ArrayUtils.contains(calculationTypes, CalculationType.DISPLAY)) {
				base = mainHandWeapon.getItemTemplate().getWeaponStats().getMeanDamage();
			} else {
				base = Rnd.get(mainHandWeapon.getItemTemplate().getWeaponStats().getMinDamage(),
						mainHandWeapon.getItemTemplate().getWeaponStats().getMaxDamage());
			}
			if (ArrayUtils.contains(calculationTypes, CalculationType.APPLY_POWER_SHARD_DAMAGE)) {
				base += getPowerShardDamage(true, ArrayUtils.contains(calculationTypes, CalculationType.REMOVE_POWER_SHARD));
			}
		}
		Stat2 stat = getStat(StatEnum.PHYSICAL_ATTACK, base, calculationTypes);
		calculationTypes = ArrayUtils.removeElement(calculationTypes, CalculationType.MAIN_HAND);
		return getStat(StatEnum.MAIN_HAND_POWER, stat, calculationTypes);
	}

	public Stat2 getOffHandPAttack(CalculationType... calculationTypes) {
		Equipment equipment = owner.getEquipment();
		Item offHandWeapon = equipment.getOffHandWeapon();
		if (offHandWeapon != null && !offHandWeapon.equals(equipment.getMainHandWeapon()) && offHandWeapon.getItemTemplate().isWeapon()) {
			calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.OFF_HAND);
			float base;
			if (ArrayUtils.contains(calculationTypes, CalculationType.DISPLAY)) {
				base = offHandWeapon.getItemTemplate().getWeaponStats().getMeanDamage();
			} else {
				base = Rnd.get(offHandWeapon.getItemTemplate().getWeaponStats().getMinDamage(),
						offHandWeapon.getItemTemplate().getWeaponStats().getMaxDamage());
			}
			if (ArrayUtils.contains(calculationTypes, CalculationType.APPLY_POWER_SHARD_DAMAGE))
				base += getPowerShardDamage(false, ArrayUtils.contains(calculationTypes, CalculationType.REMOVE_POWER_SHARD));
			Stat2 stat = getStat(StatEnum.PHYSICAL_ATTACK, base, calculationTypes);
			if (ArrayUtils.contains(calculationTypes, CalculationType.DISPLAY)) {
				stat.setBaseRate(stat.getBaseRate() * getOffHandDamageRatio());
				stat.setBonusRate(stat.getBonusRate() * getOffHandDamageRatio());
			}
			calculationTypes = ArrayUtils.removeElement(calculationTypes, CalculationType.OFF_HAND);
			return getStat(StatEnum.OFF_HAND_POWER, stat, calculationTypes);
		}
		return new AdditionStat(StatEnum.OFF_HAND_POWER, 0, owner);
	}

	@Override
	public Stat2 getMainHandMAttack(CalculationType... calculationTypes) {
		calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.MAIN_HAND);
		float base = getStatsTemplate().getMagicalAttack();
		Equipment equipment = owner.getEquipment();
		Item mainHandWeapon = equipment.getMainHandWeapon();
		if (mainHandWeapon != null) {
			if (!mainHandWeapon.getItemTemplate().getAttackType().isMagical())
				return new AdditionStat(StatEnum.MAIN_HAND_POWER, 0, owner);
			base = mainHandWeapon.getItemTemplate().getWeaponStats().getMeanDamage();
			if (ArrayUtils.contains(calculationTypes, CalculationType.APPLY_POWER_SHARD_DAMAGE))
				base += getPowerShardDamage(true, ArrayUtils.contains(calculationTypes, CalculationType.REMOVE_POWER_SHARD));
		}
		Stat2 stat = getStat(StatEnum.MAGICAL_ATTACK, base, calculationTypes);
		calculationTypes = ArrayUtils.removeElement(calculationTypes, CalculationType.MAIN_HAND);
		return getStat(StatEnum.MAIN_HAND_POWER, stat, calculationTypes);
	}

	public Stat2 getOffHandMAttack(CalculationType... calculationTypes) {
		Equipment equipment = owner.getEquipment();
		Item offHandWeapon = equipment.getOffHandWeapon();
		if (offHandWeapon != null && !offHandWeapon.equals(equipment.getMainHandWeapon()) && offHandWeapon.getItemTemplate().isWeapon()) {
			calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.OFF_HAND);
			float base = offHandWeapon.getItemTemplate().getWeaponStats().getMeanDamage();
			if (ArrayUtils.contains(calculationTypes, CalculationType.APPLY_POWER_SHARD_DAMAGE))
				base += getPowerShardDamage(false, ArrayUtils.contains(calculationTypes, CalculationType.REMOVE_POWER_SHARD));
			Stat2 stat = getStat(StatEnum.MAGICAL_ATTACK, base, calculationTypes);
			if (ArrayUtils.contains(calculationTypes, CalculationType.DISPLAY)) {
				stat.setBaseRate(stat.getBaseRate() * getOffHandDamageRatio());
				stat.setBonusRate(stat.getBonusRate() * getOffHandDamageRatio());
			}
			calculationTypes = ArrayUtils.removeElement(calculationTypes, CalculationType.OFF_HAND);
			return getStat(StatEnum.OFF_HAND_POWER, stat, calculationTypes);
		}
		return new AdditionStat(StatEnum.OFF_HAND_POWER, 0, owner);
	}

	@Override
	public Stat2 getMainHandPCritical() {
		int base = getStatsTemplate().getPcrit();
		Equipment equipment = owner.getEquipment();
		Item mainHandWeapon = equipment.getMainHandWeapon();
		if (mainHandWeapon != null && !mainHandWeapon.getItemTemplate().getAttackType().isMagical()) {
			base += mainHandWeapon.getItemTemplate().getWeaponStats().getCritical();
		}
		return getStat(StatEnum.PHYSICAL_CRITICAL, base);
	}

	public Stat2 getOffHandPCritical() {
		int base = getStatsTemplate().getPcrit();
		Equipment equipment = owner.getEquipment();
		Item offHandWeapon = equipment.getOffHandWeapon();
		if (offHandWeapon != null && !offHandWeapon.equals(equipment.getMainHandWeapon()) && offHandWeapon.getItemTemplate().isWeapon()
			&& !offHandWeapon.getItemTemplate().getAttackType().isMagical()) {
			base += offHandWeapon.getItemTemplate().getWeaponStats().getCritical();
			return getStat(StatEnum.PHYSICAL_CRITICAL, base);
		}
		return new AdditionStat(StatEnum.OFF_HAND_CRITICAL, 0, owner);
	}

	@Override
	public Stat2 getMainHandPAccuracy() {
		int base = getStatsTemplate().getAccuracy();
		Item mainHandWeapon = owner.getEquipment().getMainHandWeapon();
		if (mainHandWeapon != null) {
			base += mainHandWeapon.getItemTemplate().getWeaponStats().getPhysicalAccuracy();
		}
		return getStat(StatEnum.PHYSICAL_ACCURACY, base);
	}

	public Stat2 getOffHandPAccuracy() {
		Equipment equipment = owner.getEquipment();
		Item offHandWeapon = equipment.getOffHandWeapon();
		if (offHandWeapon != null && !offHandWeapon.equals(equipment.getMainHandWeapon()) && offHandWeapon.getItemTemplate().isWeapon()) {
			int base = getStatsTemplate().getAccuracy();
			base += offHandWeapon.getItemTemplate().getWeaponStats().getPhysicalAccuracy();
			return getStat(StatEnum.PHYSICAL_ACCURACY, base);
		}
		return new AdditionStat(StatEnum.OFF_HAND_ACCURACY, 0, owner);
	}

	@Override
	public Stat2 getMBoost() {
		int base = getStatsTemplate().getMagicBoost();
		Item mainHandWeapon = owner.getEquipment().getMainHandWeapon();
		if (mainHandWeapon != null) {
			base += mainHandWeapon.getItemTemplate().getWeaponStats().getBoostMagicalSkill();
		}
		return getStat(StatEnum.BOOST_MAGICAL_SKILL, base);
	}

	@Override
	public Stat2 getMAccuracy() {
		int base = getStatsTemplate().getMacc();
		Item mainHandWeapon = owner.getEquipment().getMainHandWeapon();
		if (mainHandWeapon != null) {
			base += mainHandWeapon.getItemTemplate().getWeaponStats().getMagicalAccuracy();
		}
		return getStat(StatEnum.MAGICAL_ACCURACY, base);
	}

	@Override
	public Stat2 getMCritical() {
		int base = getStatsTemplate().getMcrit();
		Item mainHandWeapon = owner.getEquipment().getMainHandWeapon();
		if (mainHandWeapon != null && mainHandWeapon.getItemTemplate().getAttackType().isMagical()) {
			base += mainHandWeapon.getItemTemplate().getWeaponStats().getCritical();
		}
		return getStat(StatEnum.MAGICAL_CRITICAL, base);
	}

	@SuppressWarnings("lossy-conversions")
	@Override
	public Stat2 getHpRegenRate() {
		int base = owner.getLevel() + 3;
		if (owner.isInState(CreatureState.RESTING))
			base *= 8;
		base *= getHealth().getCurrent() / 100f;
		return getStat(StatEnum.REGEN_HP, base);
	}

	@SuppressWarnings("lossy-conversions")
	@Override
	public Stat2 getMpRegenRate() {
		int base = owner.getLevel() + 8;
		if (owner.isInState(CreatureState.RESTING))
			base *= 8;
		base *= getWill().getCurrent() / 100f;
		return getStat(StatEnum.REGEN_MP, base);
	}

	@Override
	public void updateStatInfo() {
		PacketSendUtility.sendPacket(owner, new SM_STATS_INFO(owner));
	}

	@Override
	public void updateSpeedInfo() {
		PacketSendUtility.broadcastToSightedPlayers(owner, new SM_EMOTION(owner, EmotionType.CHANGE_SPEED), true);
	}

	public int getHealthDependentAdditionalHp() {
		return calculateBaseStatDependentAdditionalValue(getHealth(), owner.getPlayerClass().getHealthMultiplier());
	}

	public int getWillDependentAdditionalMp() {
		return calculateBaseStatDependentAdditionalValue(getWill(), owner.getPlayerClass().getWillMultiplier());
	}

	public int getAgilityDependentAdditionalBaseBlock() {
		return calculateBaseStatDependentAdditionalValue(getAgility(), owner.getPlayerClass().getAgilityMultiplier());
	}

	public int getAgilityDependentAdditionalBaseParry() {
		return calculateBaseStatDependentAdditionalValue(getAgility(), owner.getPlayerClass().getAgilityMultiplier());
	}

	public int getAgilityDependentAdditionalBaseEvasion() {
		return calculateBaseStatDependentAdditionalValue(getAgility(), owner.getPlayerClass().getAgilityMultiplier());
	}

	public int getAccuracyDependentAdditionalBasePhysicalAccuracy() {
		return calculateBaseStatDependentAdditionalValue(getAccuracy(), owner.getPlayerClass().getAccuracyMultiplier());
	}

	public int getAccuracyDependentAdditionalBasePhysicalCritical() {
		return calculateBaseStatDependentAdditionalValue(getAccuracy(), owner.getPlayerClass().getAccuracyMultiplier()/20);
	}

	private int calculateBaseStatDependentAdditionalValue(Stat2 baseStat, int multiplier) {
		return (int) ((baseStat.getCurrent() - 100) / 100f * multiplier);
	}

	private int getPowerShardDamage(boolean mainHand, boolean removePowerShards) {
		if (owner.isInState(CreatureState.POWERSHARD)) {
			Equipment equipment = owner.getEquipment();
			Item weapon = mainHand ? equipment.getMainHandWeapon() : equipment.getOffHandWeapon();
			Item firstShard = equipment.getMainHandPowerShard();
			Item secondShard = equipment.getOffHandPowerShard();
			if (weapon != null && weapon.getItemTemplate().getItemSubType() != ItemSubType.SHIELD) {
				int dmg = 0;
				if (mainHand) {
					if (firstShard != null) {
						dmg = firstShard.getItemTemplate().getWeaponBoost();
						if (removePowerShards)
							owner.getEquipment().usePowerShard(firstShard, 1);
					}
					if (weapon.getItemTemplate().isTwoHandWeapon() && secondShard != null) {
						dmg += secondShard.getItemTemplate().getWeaponBoost();
						if (removePowerShards)
							owner.getEquipment().usePowerShard(secondShard, 1);
					}
				} else if (secondShard != null) {
					dmg = secondShard.getItemTemplate().getWeaponBoost();
					if (removePowerShards)
						owner.getEquipment().usePowerShard(secondShard, 1);
				}
				return dmg;
			}
		}
		return 0;
	}

	public float getSkillEfficiency() {
		return skillEfficiency;
	}

	public int getMaxDamageChance() {
		return maxDamageChance;
	}

	public float getMinDamageRatio() {
		return minDamageRatio;
	}

	public void setSkillEfficiency(float skillEfficiency) {
		this.skillEfficiency = skillEfficiency;
	}

	public void setMaxDamageChance(int maxDamageChance) {
		this.maxDamageChance = maxDamageChance;
	}

	public void setMinDamageRatio(float minDamageRatio) {
		this.minDamageRatio = minDamageRatio;
	}

	public float getOffHandDamageRatio() {
		return getMinDamageRatio() * (1 - getMaxDamageChance() / 1000f) + getMaxDamageChance() / 1000f;
	}
}
