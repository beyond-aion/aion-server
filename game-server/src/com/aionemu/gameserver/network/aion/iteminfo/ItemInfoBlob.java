package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ArmorType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.PacketWriteHelper;

/**
 * Entry item info packet data (contains blob entries with detailed info).
 * 
 * @author -Nemesiss-, Rolandas
 */
public class ItemInfoBlob extends PacketWriteHelper {

	private final Player player;
	private final Item item;
	private final List<ItemBlobEntry> itemBlobEntries = new ArrayList<>();

	public ItemInfoBlob(Player player, Item item) {
		this.player = player;
		this.item = item;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeH(buf, size());
		for (ItemBlobEntry ent : itemBlobEntries)
			ent.writeMe(buf);
	}

	public void addBlobEntry(ItemBlobType type) {
		ItemBlobEntry ent = type.newBlobEntry();
		ent.setOwner(player, item, null);
		itemBlobEntries.add(ent);
	}

	public void addBonusBlobEntry(IStatFunction modifier) {
		ItemBlobEntry ent = ItemBlobType.STAT_BONUSES.newBlobEntry();
		ent.setOwner(player, item, modifier);
		itemBlobEntries.add(ent);
	}

	public static ItemBlobEntry newBlobEntry(ItemBlobType type, Player player, Item item) {
		if (type == ItemBlobType.STAT_BONUSES)
			throw new UnsupportedOperationException();
		ItemBlobEntry ent = type.newBlobEntry();
		ent.setOwner(player, item, null);
		return ent;
	}

	public static ItemInfoBlob getFullBlob(Player player, Item item) {
		ItemInfoBlob blob = new ItemInfoBlob(player, item);

		ItemTemplate itemTemplate = item.getItemTemplate();

		if (item.hasFusionedItem() || itemTemplate.isTwoHandWeapon())
			blob.addBlobEntry(ItemBlobType.COMPOSITE_ITEM);

		if (itemTemplate.getItemGroup().getValidEquipmentSlots() != 0) {
			// EQUIPPED SLOT
			blob.addBlobEntry(ItemBlobType.EQUIPPED_SLOT);

			if (itemTemplate.getItemGroup() == ItemGroup.WING) {
				blob.addBlobEntry(ItemBlobType.SLOTS_WING);
			} else if (itemTemplate.getItemGroup() == ItemGroup.SHIELD) {
				blob.addBlobEntry(ItemBlobType.SLOTS_SHIELD);
			} else if (itemTemplate.getItemGroup() == ItemGroup.PLUME) {
				blob.addBlobEntry(ItemBlobType.PLUME_INFO);
			} else if (itemTemplate.isArmor()) {
				if (itemTemplate.getItemGroup().getArmorType() == ArmorType.ACCESSORY)
					blob.addBlobEntry(ItemBlobType.SLOTS_ACCESSORY); // power shards, helmets, earrings, rings, belts
				else
					blob.addBlobEntry(ItemBlobType.SLOTS_ARMOR);
			} else if (itemTemplate.isWeapon()) {
				blob.addBlobEntry(ItemBlobType.SLOTS_WEAPON);
			}

			blob.addBlobEntry(ItemBlobType.ENCHANT_INFO);

			if (item.getConditioningInfo() != null)
				blob.addBlobEntry(ItemBlobType.CONDITIONING_INFO);

			// All items with only General
			if (blob.getBlobEntries().size() > 0) {
				if (itemTemplate.isCanPolish())
					blob.addBlobEntry(ItemBlobType.POLISH_INFO);
				blob.addBlobEntry(ItemBlobType.PREMIUM_OPTION);
			}

			List<StatFunction> allModifiers = itemTemplate.getModifiers();
			if (allModifiers != null) {
				for (IStatFunction modifier : allModifiers) {
					if (modifier.isBonus() && !modifier.hasConditions()) {
						blob.addBonusBlobEntry(modifier);
					}
				}
			}
		}

		if (itemTemplate.getItemGroup() == ItemGroup.STIGMA_SHARD)
			blob.addBlobEntry(ItemBlobType.STIGMA_SHARD);

		// GENERAL INFO
		blob.addBlobEntry(ItemBlobType.GENERAL_INFO);
		if (item.getPackCount() != 0) {
			blob.addBlobEntry(ItemBlobType.WRAP_INFO);
		}
		return blob;
	}

	public List<ItemBlobEntry> getBlobEntries() {
		return itemBlobEntries;
	}

	public int size() {
		int totalSize = 0;
		for (ItemBlobEntry ent : itemBlobEntries)
			totalSize += ent.getSize() + 1; // 1 C for blob id
		return totalSize;
	}

	public enum ItemBlobType {

		GENERAL_INFO(0x00) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new GeneralInfoBlobEntry();
			}
		},
		SLOTS_WEAPON(0x01) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new WeaponInfoBlobEntry();
			}
		},
		SLOTS_ARMOR(0x02) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new ArmorInfoBlobEntry();
			}
		},
		SLOTS_SHIELD(0x03) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new ShieldInfoBlobEntry();
			}
		},
		SLOTS_ACCESSORY(0x04) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new AccessoryInfoBlobEntry();
			}
		},
		SLOTS_ARROW(0x05) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new ArrowInfoBlobEntry();
			}
		},
		EQUIPPED_SLOT(0x06) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new EquippedSlotBlobEntry();
			}
		},
		// Removed from 3.5
		STIGMA_INFO(0x07) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new StigmaInfoBlobEntry();
			}
		},
		// Removed from 4.5, added back in 4.7
		STIGMA_SHARD(0x08) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new StigmaShardInfoBlobEntry();
			}
		},
		// missing(0x09), //15? [Not handled before]
		PREMIUM_OPTION(0x10) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new PremiumOptionInfoBlobEntry();
			}
		},
		POLISH_INFO(0x11) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new PolishInfoBlobEntry();
			}
		},
		WRAP_INFO(0x12) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new WrapInfoBlobEntry();
			}
		},
		PLUME_INFO(0x13) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new PlumeInfoBlobEntry();
			}
		},
		STAT_BONUSES(0x0A) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new BonusInfoBlobEntry();
			}
		},
		ENCHANT_INFO(0x0B) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new EnchantInfoBlobEntry();
			}
		},
		// 0x0C - not used?
		SLOTS_WING(0x0D) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new WingInfoBlobEntry();
			}
		},
		COMPOSITE_ITEM(0x0E) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new CompositeItemBlobEntry();
			}
		},
		CONDITIONING_INFO(0x0F) {

			@Override
			ItemBlobEntry newBlobEntry() {
				return new ConditioningInfoBlobEntry();
			}
		};

		private final int entryId;

		ItemBlobType(int entryId) {
			this.entryId = entryId;
		}

		public int getEntryId() {
			return entryId;
		}

		abstract ItemBlobEntry newBlobEntry();
	}
}
