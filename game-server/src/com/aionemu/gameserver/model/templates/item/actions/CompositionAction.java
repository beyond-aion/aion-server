package com.aionemu.gameserver.model.templates.item.actions;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Handles combining two enchantment stones into one via {@code CM_COMPOSITE_STONES}. Not part of the regular
 * item-use flow (retail has no server-side marker for this on the combination tool item, the client alone knows to
 * send this packet for it), so unlike other item actions this isn't XML-bound and isn't invoked through
 * {@link AbstractItemAction}.
 */
public class CompositionAction {

	public boolean canAct(Player player, Item tools, Item first, Item second) {

		if (!tools.getItemTemplate().isCombinationItem())
			return false;

		if (!first.getItemTemplate().isEnchantmentStone())
			return false;

		if (!second.getItemTemplate().isEnchantmentStone())
			return false;

		if (first.getItemCount() < 1 || second.getItemCount() < 1)
			return false;

		return first.getItemTemplate().getLevel() <= 95 && second.getItemTemplate().getLevel() <= 95;
	}

	public void act(final Player player, final Item tools, final Item first, final Item second) {
		boolean result = player.getInventory().decreaseByItemId(tools.getItemId(), 1);
		boolean result1 = player.getInventory().decreaseByItemId(first.getItemId(), 1);
		boolean result2 = player.getInventory().decreaseByItemId(second.getItemId(), 1);
		if (result && result1 && result2) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_SUCCESS(second.getL10n(), first.getL10n()));
			ItemService.addItem(player, getItemId(calcLevel(first.getItemTemplate().getLevel(), second.getItemTemplate().getLevel())), 1);
		}
	}

	private int calcLevel(int first, int second) {
		int value = ((first + second) / 2);
		if (value < 11) {
			value = Rnd.get(1, 20);
		} else {
			int random = Rnd.get(1, 10);
			int bit = Rnd.get(0, 1);
			value = (bit == 0 ? value - random : value + random);
		}
		return value;
	}

	public int getItemId(int value) {
		return 166000000 + value;
	}
}
