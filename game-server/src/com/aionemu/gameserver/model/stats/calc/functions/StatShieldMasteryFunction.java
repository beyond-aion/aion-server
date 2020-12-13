package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author VladimirZ
 */
public class StatShieldMasteryFunction extends StatRateFunction {

	public StatShieldMasteryFunction(StatEnum name, int value, boolean bonus) {
		super(name, value, bonus);
	}

	@Override
	public void apply(Stat2 stat, CalculationType... calculationTypes) {
		Player player = (Player) stat.getOwner();
		if (player.getEquipment().isShieldEquipped())
			super.apply(stat, calculationTypes);
	}
}
