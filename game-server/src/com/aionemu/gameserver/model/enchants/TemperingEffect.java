package com.aionemu.gameserver.model.enchants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.container.PlumStatEnum;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;

/**
 * @author xTz
 */
public class TemperingEffect implements StatOwner {

	private TemperingEffect(Player player, List<IStatFunction> functions) {
		player.getGameStats().addEffect(this, functions);
	}

	public void endEffect(Player player) {
		player.getGameStats().endEffect(this);
	}

	private static void addAccessoryStatFunctions(Item item, List<IStatFunction> functions) {
		Map<Integer, List<TemperingStat>> tempering = DataManager.TEMPERING_DATA.getTemplates(item.getItemTemplate());
		List<TemperingStat> temperingStats = tempering == null ? null : tempering.get(item.getTempering());
		if (temperingStats == null)
			return;
		for (TemperingStat temperingStat : temperingStats)
			functions.add(new StatAddFunction(temperingStat.getStat(), temperingStat.getValue(), false));
	}

	private static void addPlumeStatFunctions(Item item, List<IStatFunction> functions) {
		StatEnum st;
		int value = item.getRndPlumeBonusValue();
		if (item.getItemTemplate().getTemperingName().equals("TSHIRT_PHYSICAL")) {
			st = StatEnum.PHYSICAL_ATTACK;
			value += PlumStatEnum.PLUM_PHISICAL_ATTACK.getBoostValue() * item.getTempering();
		} else {
			st = StatEnum.BOOST_MAGICAL_SKILL;
			value += PlumStatEnum.PLUM_BOOST_MAGICAL_SKILL.getBoostValue() * item.getTempering();
		}
		functions.add(new StatAddFunction(st, value, true));
		functions.add(new StatAddFunction(StatEnum.MAXHP, PlumStatEnum.PLUM_HP.getBoostValue() * item.getTempering(), true));
	}

	public static void apply(Player player, Item item) {
		List<IStatFunction> functions = new ArrayList<>();
		if (item.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
			addPlumeStatFunctions(item, functions);
		} else {
			addAccessoryStatFunctions(item, functions);
		}
		if (functions.isEmpty()) {
			LoggerFactory.getLogger(TemperingEffect.class).warn("Missing tempering effect info for item " + item);
			return;
		}
		if (item.getTemperingEffect() != null)
			item.getTemperingEffect().endEffect(player);
		item.setTemperingEffect(new TemperingEffect(player, functions));
	}
}
