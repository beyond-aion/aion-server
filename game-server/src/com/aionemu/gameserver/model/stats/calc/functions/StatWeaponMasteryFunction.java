package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;

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
	public void apply(Stat2 stat) {
		Player player = (Player) stat.getOwner();
		ItemGroup mainWeapon = player.getEquipment().getMainHandWeaponType();
		ItemGroup offHandWeapon = player.getEquipment().getOffHandWeaponType();
		switch (this.stat) {
			case MAIN_HAND_POWER:
				if (mainWeapon != null && mainWeapon.equals(itemGroup)) {
					applyTo(stat);
				}
				break;
			case OFF_HAND_POWER:
				if (offHandWeapon != null && offHandWeapon.equals(itemGroup))
					applyTo(stat);
				break;
			default:
				if (mainWeapon != null && mainWeapon.equals(itemGroup))
					applyTo(stat);
		}
	}


	private void applyTo(Stat2 stat) {
		if (isBonus()) {
			int bonus = Math.round(stat.getBaseWithoutBaseRate() * getValue() / 100f);
			stat.addToBonus(bonus);
		} else {
			stat.setBase(Math.round(stat.getBaseWithoutBaseRate() * stat.calculatePercent(getValue())));
		}
	}
}
