package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.GodStone;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Avol
 * @modified ATracer, Neon
 */
public class SM_UPDATE_PLAYER_APPEARANCE extends AionServerPacket {

	private int playerId;
	private List<Item> items;

	public SM_UPDATE_PLAYER_APPEARANCE(int playerId, List<Item> items) {
		this.playerId = playerId;
		this.items = items;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerId);

		int mask = 0;
		for (Item item : items) {
			mask |= item.getEquipmentSlot();
			// remove sub hand mask bits (sub hand is present on TwoHandeds by default and would produce display bugs)
			if (ItemSlot.isTwoHandedWeapon(item.getEquipmentSlot()))
				mask &= ~(ItemSlot.SUB_HAND.getSlotIdMask() | ItemSlot.SUB_OFF_HAND.getSlotIdMask());
		}

		writeD(mask);
		for (Item item : items) {
			writeD(item.getItemSkinTemplate().getTemplateId());
			GodStone godStone = item.getGodStone();
			writeD(godStone != null ? godStone.getItemId() : 0);
			writeDyeInfo(item.getItemColor());
			writeH(item.getItemEnchantParam());
			writeH(0); // 4.7
		}
	}
}
