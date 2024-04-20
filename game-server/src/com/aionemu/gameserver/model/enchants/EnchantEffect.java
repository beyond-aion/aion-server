package com.aionemu.gameserver.model.enchants;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;

/**
 * @author xTz
 */
public class EnchantEffect implements StatOwner {

	private ItemSlot itemSlot;

	public EnchantEffect(Item item, Player player, List<EnchantStat> enchantStats) {
		List<IStatFunction> functions = new ArrayList<>();
		long itemSlot = item.getEquipmentSlot();
		for (EnchantStat enchantStat : enchantStats) {
			switch (enchantStat.getStat()) {
				case PHYSICAL_ATTACK:
				case MAGICAL_ATTACK:
					if (itemSlot == ItemSlot.MAIN_HAND.getSlotIdMask() || itemSlot == ItemSlot.MAIN_OR_SUB.getSlotIdMask())
						this.itemSlot = ItemSlot.MAIN_HAND;
					else
						this.itemSlot = ItemSlot.SUB_HAND;
					functions.add(new StatAddFunction(enchantStat.getStat(), enchantStat.getValue(), false));
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
		player.getGameStats().addEffect(this, functions);
	}

	public void endEffect(Player player) {
		player.getGameStats().endEffect(this);
	}

	public ItemSlot getItemSlot() {
		return itemSlot;
	}

}
