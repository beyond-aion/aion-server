package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.IdianStone;
import com.aionemu.gameserver.model.items.ItemStone;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.stats.container.PlumStatEnum;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob sends info about mana stones.
 * 
 * @author -Nemesiss-
 * @modified Rolandas
 */
public class ManaStoneInfoBlobEntry extends ItemBlobEntry {

  public static int size = /*8 + Item.MAX_BASIC_STONES * 4 + 4 + 13 + 5 + 5 + (18 * 4 + 1)*/138;

	ManaStoneInfoBlobEntry() {
		super(ItemBlobType.MANA_SOCKETS);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		Item item = ownerItem;
		int optionalSocket = item.getOptionalSocket();
		writeC(buf, item.isSoulBound() ? 1 : 0);
		writeC(buf, item.getEnchantLevel()); // enchant (1-15)
		writeD(buf, item.getItemSkinTemplate().getTemplateId());
		writeC(buf, item.getRandomCount() == -1 ? -1 : optionalSocket);
		writeC(buf, item.getRandomCount() == -1 ? -1 : item.getEnchantBonus());

		writeItemStones(buf);
		
		ItemStone god = item.getGodStone();
		writeD(buf, god == null ? 0 : god.getItemId());
		
		int itemColor = item.getItemColor();
		int dyeExpiration = item.getColorTimeLeft();
		// expired dyed items
		if ((dyeExpiration > 0 && item.getColorExpireTime() > 0 || dyeExpiration == 0 && item.getColorExpireTime() == 0)
			&& item.getItemTemplate().isItemDyePermitted()) {
			writeC(buf, itemColor == 0 ? 0 : 1);
			writeD(buf, itemColor);
			writeD(buf, 0); // unk 1.5.1.9
			writeD(buf, dyeExpiration); // seconds until dye expires
		}
		else {
			writeC(buf, 0);
			writeD(buf, 0);
			writeD(buf, 0); // unk 1.5.1.9
			writeD(buf, 0);
		}
		
		IdianStone idianStone = item.getIdianStone();
		if (idianStone != null && idianStone.getPolishNumber() > 0) {
			writeD(buf, idianStone.getItemId()); // Idian Stone template ID
			writeC(buf, idianStone.getPolishNumber()); // polish statset ID
		}
		else {
			writeD(buf, 0); // Idian Stone template ID
			writeC(buf, 0); // polish statset ID
		}
		
		writeC(buf, item.getTempering()); // tempering level
		

		writeD(buf, 0x00);
		writeC(buf, 0x00);
		writeD(buf, 0x00);
		writeC(buf, 0x00);
		writeD(buf, 0x00);
		writeD(buf, 0x00);
		
		
		if (item.getTempering() > 0 && item.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
			writeD(buf, PlumStatEnum.PLUM_HP.getId()); //1satId Plum
			writeD(buf, PlumStatEnum.PLUM_HP.getBoostValue() * item.getTempering()); //value
		}
		else {
			writeD(buf, 0x00); //1satId Plum
			writeD(buf, 0x00); //value
		}
		if (item.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
			if (item.getTempering() > 0) {
				PlumStatEnum stat = item.getItemTemplate().getTemperingName().equals("TSHIRT_PHYSICAL") ? PlumStatEnum.PLUM_PHISICAL_ATTACK : PlumStatEnum.PLUM_BOOST_MAGICAL_SKILL;
				writeD(buf, stat.getId());//2satId Plum
				writeD(buf, stat.getBoostValue() * item.getTempering());
			}
			else {
				writeD(buf, 0x00);//2satId Plum
				writeD(buf, 0x00);
			}
		}
		else {
			writeD(buf, 0x00);//2satId Plum
			writeD(buf, 0x00);
		}
		writeD(buf, 0x00);//3satId Plum
		writeD(buf, 0x00);
		writeD(buf, 0x00);//4satId Plum
		writeD(buf, 0x00);
		writeD(buf, 0x00);//5satId Plum
		writeD(buf, 0x00);
		writeD(buf, 0x00);//6satId Plum
		writeD(buf, 0x00);
		writeD(buf, 0x00);//unk 4.7.5
		writeC(buf, item.isAmplified() ? 1 : 0);
		writeD(buf, item.getBuffSkill());
		writeD(buf, 0x00);//skillId
		writeD(buf, 0x00);//skillId
	}

	/**
	 * Writes manastones
	 * 
	 * @param item
	 */
	private void writeItemStones(ByteBuffer buf) {
		Item item = ownerItem;

		if (item.hasManaStones()) {
			Set<ManaStone> itemStones = item.getItemStones();
			HashMap<Integer, ManaStone> stonesBySlot = new HashMap<>();
			for (ManaStone itemStone : itemStones) {
				stonesBySlot.put(itemStone.getSlot(), itemStone);
			}
			for (int i = 0; i < Item.MAX_BASIC_STONES; i++) {
				ManaStone stone = stonesBySlot.get(i);
				if (stone == null)
					writeD(buf, 0);
				else
					writeD(buf, stone.getItemId());
			}
		}
		else {
			skip(buf, Item.MAX_BASIC_STONES * 4);
		}
	}

	@Override
	public int getSize() {
		return size;
	}
}
