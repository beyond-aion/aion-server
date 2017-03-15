package com.aionemu.gameserver.model.enchants;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.container.PlumStatEnum;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author xTz
 */
public class TemperingEffect implements StatOwner {

	private List<IStatFunction> functions = new ArrayList<>();

	/**
	 * @TODO Rework Bad code. For plums
	 * @param player
	 * @param item
	 */
	public TemperingEffect(Player player, Item item) {
		StatEnum st;
		int value;
		if (item.getItemTemplate().getTemperingName().equals("TSHIRT_PHYSICAL")) {
			st = StatEnum.PHYSICAL_ATTACK;
			value = PlumStatEnum.PLUM_PHISICAL_ATTACK.getBoostValue() * item.getTempering();
		} else {
			st = StatEnum.BOOST_MAGICAL_SKILL;
			value = PlumStatEnum.PLUM_BOOST_MAGICAL_SKILL.getBoostValue() * item.getTempering();
		}
		value += item.getRndPlumeBonusValue();
		functions.add(new StatAddFunction(st, value, true));
		functions.add(new StatAddFunction(StatEnum.MAXHP, PlumStatEnum.PLUM_HP.getBoostValue() * item.getTempering(), true));
		player.getGameStats().addEffect(TemperingEffect.this, functions);
	}

	public TemperingEffect(Player player, List<TemperingStat> temperingStats) {
		for (TemperingStat temperingStat : temperingStats) {
			functions.add(new StatAddFunction(temperingStat.getStat(), temperingStat.getValue(), false));
		}
		player.getGameStats().addEffect(TemperingEffect.this, functions);
	}

	public void endEffect(Player player) {
		functions.clear();
		player.getGameStats().endEffect(this);
	}

}
