package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer (based on Mr.Poke ArmorMasteryModifier)
 */
public class StatArmorMasteryFunction extends StatRateFunction {

	private final ItemSubType subGroup;

	public StatArmorMasteryFunction(ItemSubType subGroup, StatEnum name, int value, boolean bonus) {
		super(name, value, bonus);
		this.subGroup = subGroup;
	}

	@Override
	public void apply(Stat2 stat, CalculationType... calculationTypes) {
		Player player = (Player) stat.getOwner();
		if (player.getEquipment().isArmorEquipped(subGroup))
			super.apply(stat, calculationTypes);
	}
}
