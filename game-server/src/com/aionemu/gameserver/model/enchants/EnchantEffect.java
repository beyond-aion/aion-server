package com.aionemu.gameserver.model.enchants;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author xTz
 */
public class EnchantEffect implements StatOwner {

	private List<IStatFunction> functions = new FastTable<>();

	public EnchantEffect(Item item, Player player, List<EnchantStat> enchantStats) {
		Long itemSlot = item.getEquipmentSlot();
		for (EnchantStat enchantStat : enchantStats) {
			switch (enchantStat.getStat()) {
				case PHYSICAL_ATTACK:
				case MAGICAL_ATTACK:
					StatEnum stat = null;
					if (itemSlot == ItemSlot.MAIN_HAND.getSlotIdMask() || itemSlot == ItemSlot.MAIN_OR_SUB.getSlotIdMask())
						stat = StatEnum.MAIN_HAND_POWER;
					else
						stat = StatEnum.OFF_HAND_POWER;
					functions.add(new StatAddFunction(stat, enchantStat.getValue(), false));
					break;
				case BOOST_MAGICAL_SKILL:
					if (itemSlot == ItemSlot.MAIN_HAND.getSlotIdMask() || itemSlot == ItemSlot.MAIN_OR_SUB.getSlotIdMask())
						functions.add(new StatAddFunction(enchantStat.getStat(), enchantStat.getValue(), false));
					break;
				default:
					functions.add(new StatAddFunction(enchantStat.getStat(), enchantStat.getValue(), false));
					break;
			}
		}
		player.getGameStats().addEffect(EnchantEffect.this, functions);
	}

	public void endEffect(Player player) {
		functions.clear();
		player.getGameStats().endEffect(this);
	}

}
