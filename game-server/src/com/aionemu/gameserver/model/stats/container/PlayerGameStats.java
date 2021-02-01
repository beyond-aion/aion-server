package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.stats.calc.AdditionStat;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.templates.ride.RideInfo;
import com.aionemu.gameserver.model.templates.stats.StatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xavier
 */
public class PlayerGameStats extends CreatureGameStats<Player> {

	private int cachedSpeed;
	private int cachedAttackSpeed;

	/**
	 * @param owner
	 */
	public PlayerGameStats(Player owner) {
		super(owner);
	}

	@Override
	protected void onStatsChange() {
		updateStatsAndSpeedVisually();
	}

	public void updateStatsAndSpeedVisually() {
		super.onStatsChange();
		updateStatsVisually();
		checkSpeedStats();
	}

	public void updateStatsVisually() {
		updateStatInfo();
	}

	private void checkSpeedStats() {
		int current = getMovementSpeed().getCurrent();
		int currentAttackSpeed = getAttackSpeed().getCurrent();
		if (current != cachedSpeed || currentAttackSpeed != cachedAttackSpeed) {
			updateSpeedInfo();
			cachedSpeed = current;
			cachedAttackSpeed = currentAttackSpeed;
		}
	}

	@Override
	public StatsTemplate getStatsTemplate() {
		return owner.getPlayerClass().getStatsTemplateFor(owner.getLevel());
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
		Stat2 aSpeed = getStat(StatEnum.ATTACK_SPEED, base);
		return aSpeed;
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
		if (mainHandWeapon != null) {
			minWeaponRange = Math.min(minWeaponRange, mainHandWeapon.getItemTemplate().getWeaponStats().getAttackRange());
		}
		if (offHandWeapon != null && !equipment.isShieldEquipped()) {
			minWeaponRange = Math.min(minWeaponRange, offHandWeapon.getItemTemplate().getWeaponStats().getAttackRange());
		}
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
	public Stat2 getMainHandPAttack() {
		int base = getStatsTemplate().getAttack();
		Equipment equipment = owner.getEquipment();
		Item mainHandWeapon = equipment.getMainHandWeapon();
		if (mainHandWeapon != null) {
			if (mainHandWeapon.getItemTemplate().getAttackType().isMagical())
				return new AdditionStat(StatEnum.MAIN_HAND_POWER, 0, owner);
			base = mainHandWeapon.getItemTemplate().getWeaponStats().getMeanDamage();
		}
		Stat2 stat = getStat(StatEnum.PHYSICAL_ATTACK, base);
		return getStat(StatEnum.MAIN_HAND_POWER, stat);
	}

	public Stat2 getOffHandPAttack() {
		Equipment equipment = owner.getEquipment();
		Item offHandWeapon = equipment.getOffHandWeapon();
		if (offHandWeapon != null && !offHandWeapon.equals(equipment.getMainHandWeapon()) && offHandWeapon.getItemTemplate().isWeapon()) {
			int base = offHandWeapon.getItemTemplate().getWeaponStats().getMeanDamage();
			Stat2 stat = getStat(StatEnum.PHYSICAL_ATTACK, base);
			stat.setBaseRate(stat.getBaseRate() * 0.98f);
			return getStat(StatEnum.OFF_HAND_POWER, stat);
		}
		return new AdditionStat(StatEnum.OFF_HAND_POWER, 0, owner);
	}

	@Override
	public Stat2 getMainHandMAttack() {
		int base = getStatsTemplate().getMagicalAttack();
		Equipment equipment = owner.getEquipment();
		Item mainHandWeapon = equipment.getMainHandWeapon();
		if (mainHandWeapon != null) {
			if (!mainHandWeapon.getItemTemplate().getAttackType().isMagical())
				return new AdditionStat(StatEnum.MAIN_HAND_POWER, 0, owner);
			base += mainHandWeapon.getItemTemplate().getWeaponStats().getMeanDamage();
		}
		Stat2 stat = getStat(StatEnum.MAGICAL_ATTACK, base);
		return getStat(StatEnum.MAIN_HAND_POWER, stat);
	}

	public Stat2 getOffHandMAttack() {
		Equipment equipment = owner.getEquipment();
		Item offHandWeapon = equipment.getOffHandWeapon();
		if (offHandWeapon != null && !offHandWeapon.equals(equipment.getMainHandWeapon()) && offHandWeapon.getItemTemplate().isWeapon()) {
			int base = offHandWeapon.getItemTemplate().getWeaponStats().getMeanDamage();
			Stat2 stat = getStat(StatEnum.MAGICAL_ATTACK, base);
			stat.setBaseRate(stat.getBaseRate() * 0.82f);
			return getStat(StatEnum.OFF_HAND_POWER, stat);
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

	@Override
	public Stat2 getHpRegenRate() {
		int base = owner.getLevel() + 3;
		if (owner.isInState(CreatureState.RESTING))
			base *= 8;
		base *= getHealth().getCurrent() / 100f;
		return getStat(StatEnum.REGEN_HP, base);
	}

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
		PacketSendUtility.broadcastToSightedPlayers(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2), true);
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
}
