package com.aionemu.gameserver.model.stats.calc.functions;

import org.apache.commons.lang3.ArrayUtils;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer (based on Mr.Poke WeaponMasteryModifier)
 */
public class StatWeaponMasteryFunction extends StatRateFunction {

	private final ItemGroup itemGroup;

	public StatWeaponMasteryFunction(ItemGroup itemGroup, StatEnum name, int value, boolean bonus) {
		super(name, value, bonus);
		this.itemGroup = itemGroup;
	}

	@Override
	public void apply(Stat2 stat, CalculationType... calculationTypes) {
		Player player = (Player) stat.getOwner();
		ItemGroup mainWeapon = player.getEquipment().getMainHandWeaponType();
		ItemGroup offHandWeapon = player.getEquipment().getOffHandWeaponType();
		switch (this.stat) {
			case MAIN_HAND_POWER:
				if (mainWeapon != null && mainWeapon.equals(itemGroup)) {
					applyTo(stat, calculationTypes);
				}
				break;
			case OFF_HAND_POWER:
				if (offHandWeapon != null && offHandWeapon.equals(itemGroup))
					applyTo(stat, calculationTypes);
				break;
			default:
				if (mainWeapon != null && mainWeapon.equals(itemGroup))
					applyTo(stat, calculationTypes);
		}
	}


	private void applyTo(Stat2 stat, CalculationType... calculationTypes) {
		if (isBonus()) {
			int bonusRate = getValue();
			if (ArrayUtils.contains(calculationTypes, CalculationType.SKILL) && ArrayUtils.contains(calculationTypes, CalculationType.DUAL_WIELD)) {
				bonusRate = Rnd.get(0, getValue());
			}
			stat.setFixedBonusRate(bonusRate / 100f);
		} else {
			// TODO: Check if calculations differ if its not a bonus type.
			stat.setBase(stat.getExactBaseWithoutBaseRate() * stat.calculatePercent(getValue()));
		}
	}
}
