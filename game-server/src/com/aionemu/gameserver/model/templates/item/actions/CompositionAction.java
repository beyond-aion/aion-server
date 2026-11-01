package com.aionemu.gameserver.model.templates.item.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Combines two enchantment stones into one. The client opens the combination window by itself and sends {@code CM_COMPOSITE_STONES}, so
 * using the tool directly does nothing; the element only marks the item as a combination tool.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompositionAction")
public class CompositionAction extends AbstractItemAction {

	private static final int MAX_STONE_LEVEL = 95;
	private static final int MAX_CASH_LEVEL_DIFFERENCE = 20;
	private static final int MAX_RESULT_LEVEL = 125;
	// compound_enchant_stone.xml
	private static final int PROBABILITY_SETTING = 500, MIN_LEVEL_SETTING = 400, MAX_LEVEL_SETTING = 300, RANGE_SETTING = 29, KINAH_SETTING = 1;

	/**
	 * Cash tools pick from a range above the higher stone but refuse stones too far apart.
	 */
	@XmlAttribute
	private boolean cash;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		return false;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
	}

	public boolean canCompose(Player player, Item first, Item second) {
		if (!first.getItemTemplate().isEnchantmentStone() || !second.getItemTemplate().isEnchantmentStone())
			return false;
		if (first.getItemCount() < 1 || second.getItemCount() < 1)
			return false;
		int firstLevel = first.getItemTemplate().getLevel(), secondLevel = second.getItemTemplate().getLevel();
		if (firstLevel > MAX_STONE_LEVEL || secondLevel > MAX_STONE_LEVEL) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_COMPOUND_ENCHANTSTONE_CANT_LEVEL(MAX_STONE_LEVEL));
			return false;
		}
		if (cash && Math.abs(firstLevel - secondLevel) > MAX_CASH_LEVEL_DIFFERENCE) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_COMPOUND2_ENCHANTSTONE_CANT_LEVEL());
			return false;
		}
		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_FULL_INVENTORY());
			return false;
		}
		return true;
	}

	public void compose(Player player, Item tool, Item first, Item second) {
		int firstLevel = first.getItemTemplate().getLevel(), secondLevel = second.getItemTemplate().getLevel();
		if (!player.getInventory().decreaseByObjectId(tool.getObjectId(), 1))
			return;
		if (!player.getInventory().decreaseByObjectId(first.getObjectId(), 1))
			return;
		if (!player.getInventory().decreaseByObjectId(second.getObjectId(), 1))
			return;
		int level = cash ? calculateCashLevel(firstLevel, secondLevel) : calculateLevel(firstLevel, secondLevel);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_COMPOUND_ENCHANTSTONE_COMPLETE(first.getL10n(), second.getL10n()));
		ItemService.addItem(player, getEnchantmentStoneId(level), 1);
	}

	static int calculateLevel(int firstLevel, int secondLevel) {
		int low = Math.min(firstLevel, secondLevel), high = Math.max(firstLevel, secondLevel);
		int top = (int) (low * PROBABILITY_SETTING / 1000.0 + MAX_RESULT_LEVEL);
		int bottom = (int) (high - high * MIN_LEVEL_SETTING / 1000.0);
		int offset = rollOffset(top - (int) (low * MAX_LEVEL_SETTING / 1000.0 + high), top - bottom);
		return Math.clamp(Math.max(top - offset, bottom), 1, MAX_RESULT_LEVEL);
	}

	static int calculateCashLevel(int firstLevel, int secondLevel) {
		int low = Math.min(firstLevel, secondLevel), high = Math.max(firstLevel, secondLevel);
		int top = RANGE_SETTING + KINAH_SETTING + high;
		int start = (int) ((100.0 - (high - low) * 4.0) / 100.0 * low + MAX_STONE_LEVEL + RANGE_SETTING + KINAH_SETTING);
		int offset = rollOffset(start - top, start - (KINAH_SETTING + high));
		return Math.clamp(Math.max(start - offset, KINAH_SETTING + high), 1, top);
	}

	/**
	 * Picks an offset from [from, to], each value weighted by itself, so bigger offsets (lower results) are likelier.
	 */
	private static int rollOffset(int from, int to) {
		double threshold = Rnd.get(0, 10000) * ((double) to * (to + 1) * 0.5 - (double) from * (from - 1) * 0.5) / 10000;
		double sum = 0;
		int offset = from;
		for (; offset <= to; offset++) {
			sum += offset;
			if (sum > threshold)
				break;
		}
		return offset;
	}

	private static int getEnchantmentStoneId(int level) {
		return 166000000 + level;
	}
}
