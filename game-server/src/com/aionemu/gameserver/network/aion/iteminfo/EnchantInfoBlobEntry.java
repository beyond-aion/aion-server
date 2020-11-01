package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.IdianStone;
import com.aionemu.gameserver.model.items.ItemStone;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.stats.container.PlumStatEnum;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob sends info about enchantment, bonus attributes, mana stones, god stone etc.
 * 
 * @author -Nemesiss-, Rolandas
 */
public class EnchantInfoBlobEntry extends ItemBlobEntry {

	public static int SIZE = 138; // 8 + Item.MAX_BASIC_STONES * 4 + 4 + 13 + 5 + 5 + (18 * 4 + 1)

	EnchantInfoBlobEntry() {
		super(ItemBlobType.ENCHANT_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		writeInfo(buf, ownerItem);
	}

	public static void writeInfo(ByteBuffer buf, Item item) {
		writeInfo(buf, item, !item.isIdentified() ? -1 : item.getOptionalSockets(), !item.isIdentified() ? -1 : item.getEnchantBonus());
	}

	public static void writeInfo(ByteBuffer buf, Item item, int optionalManastoneSockets, int enchantBonus) {
		writeC(buf, item.isSoulBound() ? 1 : 0);
		writeC(buf, item.getEnchantLevel()); // enchant (1-15)
		writeD(buf, item.getItemSkinTemplate().getTemplateId());
		writeC(buf, optionalManastoneSockets);
		writeC(buf, enchantBonus);

		Map<Integer, ManaStone> stonesBySlot = createManastoneMap(item);
		for (int i = 0; i < Item.MAX_BASIC_STONES; i++) {
			ManaStone stone = stonesBySlot.get(i);
			writeD(buf, stone == null ? 0 : stone.getItemId());
		}

		writeD(buf, item.getGodStoneId());

		int dyeExpiration = item.getColorTimeLeft();
		writeDyeInfo(buf, dyeExpiration < 0 ? null : item.getItemColor());
		writeC(buf, 0); // unk (0)
		writeD(buf, 0); // unk 1.5.1.9
		writeD(buf, Math.max(0, dyeExpiration)); // seconds until dye expires

		IdianStone idianStone = item.getIdianStone();
		if (idianStone != null && idianStone.getPolishNumber() > 0) {
			writeD(buf, idianStone.getItemId()); // Idian Stone template ID
			writeC(buf, idianStone.getPolishNumber()); // polish statset ID
		} else {
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
			PlumStatEnum stat = item.getItemTemplate().getTemperingName().equals("TSHIRT_PHYSICAL") ? PlumStatEnum.PLUM_PHISICAL_ATTACK
				: PlumStatEnum.PLUM_BOOST_MAGICAL_SKILL;
			writeD(buf, PlumStatEnum.PLUM_HP.getId()); // 1st satId
			writeD(buf, PlumStatEnum.PLUM_HP.getBoostValue() * item.getTempering()); // value
			writeD(buf, stat.getId()); // 2nd statId
			writeD(buf, (stat.getBoostValue() * item.getTempering()) + item.getRndPlumeBonusValue()); // value
		} else {
			writeD(buf, 0x00); // 1st statId
			writeD(buf, 0x00); // value
			writeD(buf, 0x00); // 2nd statId
			writeD(buf, 0x00); // value
		}
		writeD(buf, 0x00); // 3rd statId
		writeD(buf, 0x00);
		writeD(buf, 0x00); // 4th statId
		writeD(buf, 0x00);
		writeD(buf, 0x00); // 5th statId
		writeD(buf, 0x00);
		writeD(buf, 0x00); // 6th statId
		writeD(buf, 0x00);
		writeD(buf, 0x00); // unk 4.7.5
		writeC(buf, item.isAmplified() ? 1 : 0);
		writeD(buf, item.getBuffSkill());
		writeD(buf, 0x00); // skillId
		writeD(buf, 0x00); // skillId
	}

	private static Map<Integer, ManaStone> createManastoneMap(Item item) {
		if (item.hasManaStones())
			return item.getItemStones().stream().collect(Collectors.toMap(ItemStone::getSlot, s -> s));
		return Collections.emptyMap();
	}

	@Override
	public int getSize() {
		return SIZE;
	}
}
