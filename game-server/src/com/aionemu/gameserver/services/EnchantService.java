package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.enchants.EnchantEffect;
import com.aionemu.gameserver.model.enchants.EnchantStat;
import com.aionemu.gameserver.model.enchants.EnchantmentStone;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.stats.listeners.ItemEquipmentListener;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.EnchantItemAction;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemSocketService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, Wakizashi, Source, vlog, Neon
 */
public class EnchantService {

	public static boolean breakItem(Player player, Item targetItem, Item parentItem) {
		Storage inventory = player.getInventory();
		if (inventory.getItemByObjId(targetItem.getObjectId()) == null || inventory.getItemByObjId(parentItem.getObjectId()) == null)
			return false;

		ItemTemplate itemTemplate = targetItem.getItemTemplate();
		if (!itemTemplate.isArmor() && !itemTemplate.isWeapon()) {
			AuditLogger.log(player, "tried to break down incompatible item type");
			return false;
		}

		int effectiveLevel = calculateEffectiveLevel(itemTemplate.getItemQuality(), itemTemplate.getLevel());
		if (effectiveLevel == 0)
			throw new IllegalArgumentException("Invalid item quality for breaking item " + itemTemplate.getItemQuality());

		int rndEffectiveLevel = effectiveLevel + Rnd.get(0, 10);
		if (itemTemplate.isWeapon())
			rndEffectiveLevel += 5;

		// Omega Stones are limited to Drops
		int stoneId;
		if (rndEffectiveLevel >= calculateEffectiveLevel(EnchantmentStone.EPSILON))
			stoneId = 166000195;
		else if (rndEffectiveLevel >= calculateEffectiveLevel(EnchantmentStone.DELTA))
			stoneId = 166000194;
		else if (rndEffectiveLevel >= calculateEffectiveLevel(EnchantmentStone.GAMMA))
			stoneId = 166000193;
		else if (rndEffectiveLevel >= calculateEffectiveLevel(EnchantmentStone.BETA))
			stoneId = 166000192;
		else
			stoneId = 166000191; // Alpha

		if (inventory.delete(targetItem) != null) {
			if (inventory.decreaseByObjectId(parentItem.getObjectId(), 1))
				ItemService.addItem(player, stoneId, itemTemplate.isWeapon() ? Rnd.get(2, 5) : Rnd.get(1, 3));
		} else
			AuditLogger.log(player, "possibly used break item hack");
		return true;
	}

	private static int calculateEffectiveLevel(EnchantmentStone enchantmentStone) {
		return calculateEffectiveLevel(enchantmentStone.getBaseQuality(), enchantmentStone.getBaseLevel());
	}

	private static int calculateEffectiveLevel(ItemQuality itemQuality, int itemLevel) {
		switch (itemQuality) {
			case COMMON: // same as rare, since there's no EnchantmentStone enum having COMMON as a base
			case RARE:
				return itemLevel + 5;
			case LEGEND:
				return itemLevel + 10;
			case UNIQUE:
				return itemLevel + 15;
			case EPIC:
				return itemLevel + 20;
			case MYTHIC:
				return itemLevel + 25;
			default:
				return 0;
		}
	}

	public static boolean enchantItem(Player player, Item enchantmentStoneItem, Item targetItem, Item supplementItem) {
		float successChance;

		if (targetItem.isAmplified())
			successChance = Rates.get(player, RatesConfig.ENCHANTMENT_STONE_AMPLIFIED_CHANCES);
		else {
			successChance = Rates.get(player, RatesConfig.ENCHANTMENT_STONE_BASE_CHANCES);

			EnchantmentStone enchantmentStone = EnchantmentStone.getByItemId(enchantmentStoneItem.getItemId());
			int itemLevel = targetItem.getItemTemplate().getLevel();
			if (itemLevel < EnchantmentStone.ALPHA.getBaseLevel()) // ensure low lvl items don't get too high success chances
				itemLevel = EnchantmentStone.ALPHA.getBaseLevel();
			int stoneToItemLevelDiff = enchantmentStone.getBaseLevel() - itemLevel;
			int stoneToItemQualityDiff = enchantmentStone.getBaseQuality().getQualityId() - targetItem.getItemTemplate().getItemQuality().getQualityId();

			successChance += stoneToItemLevelDiff; // absolutely increase/reduce chance by 1% for every level difference
			successChance += stoneToItemQualityDiff * 5; // absolutely increase/reduce chance by 5% for each quality difference

			if (targetItem.getEnchantLevel() == 0) // boost enchant chance for +1 by 20%
				successChance *= 1.2f;
			else if (targetItem.getEnchantLevel() < 5) // boost enchant chance up to +5 by 10%
				successChance *= 1.1f;
			else if (targetItem.getEnchantLevel() >= 10) // reduce enchant chance from +10 by 10%
				successChance *= 0.9f;

			// Retail Tests: 80% = Success Cap for Enchanting without Supplements
			if (successChance >= 80)
				successChance = 80;

			// Supplement is used
			if (supplementItem != null) {
				// Amount of supplement items
				int supplementUseCount = 1;
				// Additional success rate for the supplement
				ItemTemplate supplementTemplate = supplementItem.getItemTemplate();

				EnchantItemAction action = supplementTemplate.getActions().getEnchantAction();
				if (action != null) {
					if (action.isManastoneOnly())
						return false;
					// Add success rate of the supplement to the overall chance
					successChance += action.getChance();
				}

				action = enchantmentStoneItem.getItemTemplate().getActions().getEnchantAction();
				if (action != null)
					supplementUseCount = action.getCount();

				// Beginning from enchanting to +11, there are 2 times more supplements required
				if (targetItem.getEnchantLevel() >= 10)
					supplementUseCount = supplementUseCount * 2;

				// Check the required amount of the supplements
				if (player.getInventory().getItemCountByItemId(supplementTemplate.getTemplateId()) < supplementUseCount)
					return false;

				// Put supplements to wait for update
				player.subtractSupplements(supplementUseCount, supplementTemplate.getTemplateId());

				// Success can't be higher than 95%
				if (successChance >= 95)
					successChance = 95;
			}
		}

		boolean result = Rnd.chance() < successChance;

		if (player.hasAccess(AdminConfig.ENCHANT_INFO))
			PacketSendUtility.sendMessage(player, (result ? "Success" : "Fail") + " (success chance:" + successChance + "%)");

		return result;
	}

	public static void enchantItemAct(Player player, Item parentItem, Item targetItem, Item supplementItem, int currentEnchant, boolean success) {
		int addLevel = 1;

		int maxEnchant = targetItem.getItemTemplate().getMaxEnchantLevel(); // max enchant level from item_templates
		maxEnchant += targetItem.getEnchantBonus();
		if (targetItem.getEnchantLevel() < 20) {
			float chance = Rnd.chance(); // crit modifier
			if (chance < 5)
				addLevel = 3;
			else if (chance < 10)
				addLevel = 2;
		}

		if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
			AuditLogger.log(player, "possibly used enchant hack");
			return;
		}
		// Decrease required supplements
		player.updateSupplements();

		// Items that are Fabled or Eternal can get up to +15.
		if (success) {
			if (!targetItem.isAmplified() && currentEnchant + addLevel > maxEnchant)
				currentEnchant = maxEnchant;
			else
				currentEnchant += addLevel;
		} else {
			// Retail: http://powerwiki.na.aiononline.com/aion/Patch+Notes:+1.9.0.1
			// When socketing fails at +11~+15, the value falls back to +10.
			if (targetItem.isAmplified()) {
				currentEnchant = maxEnchant;
				targetItem.setAmplified(false);
				if (targetItem.getBuffSkill() != 0) {
					SkillLearnService.removeSkill(player, targetItem.getBuffSkill());
					targetItem.setBuffSkill(0);
				}
			} else if ((currentEnchant > 10 && maxEnchant > 10)) {
				currentEnchant = 10;
			} else if (currentEnchant > 0) {
				currentEnchant -= 1;
			}
		}

		if (targetItem.isAmplified())
			maxEnchant = 255;

		targetItem.setEnchantLevel(Math.min(currentEnchant, maxEnchant));
		int enchantLevel = targetItem.getEnchantLevel();

		if (targetItem.getEnchantEffect() != null) {
			targetItem.getEnchantEffect().endEffect(player);
			targetItem.setEnchantEffect(null);
		}

		if (enchantLevel >= 20) {
			int buffId = getEquipBuff(targetItem);
			int oldBuffId = targetItem.getBuffSkill();
			if (buffId != oldBuffId) {
				targetItem.setBuffSkill(buffId);
				if (targetItem.isEquipped()) {
					if (oldBuffId != 0)
						SkillLearnService.removeSkill(player, oldBuffId);
					SkillLearnService.learnTemporarySkill(player, buffId, 1);
				}
			}
			// targetItem.setPackCount(targetItem.getPackCount() + 1);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_SKILL_ENCHANT(targetItem.getL10n(), enchantLevel,
				DataManager.SKILL_DATA.getSkillTemplate(buffId).getL10n()));
		}

		if (targetItem.isEquipped()) {
			player.getGameStats().updateStatsVisually();
			if (enchantLevel > 0)
				applyEnchantEffect(targetItem, player, enchantLevel);
		}

		ItemPacketService.updateItemAfterInfoChange(player, targetItem, ItemUpdateType.STATS_CHANGE);

		if (success)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENCHANT_ITEM_SUCCEED_NEW(targetItem.getL10n(), enchantLevel));
		else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_FAILED(targetItem.getL10n()));
			if (targetItem.getItemTemplate().getEnchantType() > 0) {
				if (targetItem.isEquipped())
					player.getEquipment().decreaseEquippedItemCount(targetItem.getObjectId(), 1);
				else
					player.getInventory().decreaseByObjectId(targetItem.getObjectId(), 1);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENCHANT_TYPE1_ENCHANT_FAIL(targetItem.getL10n()));
			} else {
				targetItem.removeRemainingTuningCountIfPossible();
			}
		}
		if (targetItem.getPersistentState() != PersistentState.DELETED) {
			if (targetItem.isEquipped())
				player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
			else
				player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
	}

	public static void applyEnchantEffect(Item targetItem, Player owner, int enchantLevel) {
		Map<Integer, List<EnchantStat>> enchant = DataManager.ENCHANT_DATA.getTemplates(targetItem.getItemTemplate());
		if (enchant == null)
			return;
		int maxTemplateLevel = enchant.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
		List<EnchantStat> stats;
		if (enchantLevel > maxTemplateLevel && maxTemplateLevel < 21) // usually only test templates have max level < 21
			throw new IllegalArgumentException("Missing bonus stats for +" + enchantLevel + " (item:" + targetItem.getItemId() + ") in enchant templates");
		else if (enchantLevel < maxTemplateLevel)
			stats = enchant.get(enchantLevel);
		else {
			// maxTemplateLevel - 1 (second to last template entry) = maximum stats
			stats = new ArrayList<>(enchant.get(maxTemplateLevel - 1));
			// maxTemplateLevel (last template entry) = bonus stats per level above max
			List<EnchantStat> limitlessBoni = enchant.get(maxTemplateLevel);
			for (int i = 0; i <= enchantLevel - maxTemplateLevel; i++)
				stats.addAll(limitlessBoni);
		}
		targetItem.setEnchantEffect(new EnchantEffect(targetItem, owner, stats));
	}

	public static boolean socketManastone(Player player, Item manastone, Item targetItem, Item supplementItem, int fusionedWeaponLevel) {
		int targetItemLevel;

		// Fusioned weapon. Primary weapon level.
		if (fusionedWeaponLevel == 1)
			targetItemLevel = targetItem.getItemTemplate().getLevel();
		// Fusioned weapon. Secondary weapon level.
		else
			targetItemLevel = targetItem.getFusionedItemTemplate().getLevel();

		int stoneLevel = manastone.getItemTemplate().getLevel();
		int slotLevel = (int) (10 * Math.ceil((targetItemLevel + 10) / 10d));

		// The current amount of socketed stones
		int stoneCount;

		// Manastone level shouldn't be greater as 20 + item level
		// Example: item level: 1 - 10. Manastone level should be <= 20
		if (stoneLevel > slotLevel)
			return false;

		// Fusioned weapon. Primary weapon slots.
		if (fusionedWeaponLevel == 1)
			// Count the inserted stones in the primary weapon
			stoneCount = targetItem.getItemStones().size();
		// Fusioned weapon. Secondary weapon slots.
		else
			// Count the inserted stones in the secondary weapon
			stoneCount = targetItem.getFusionStones().size();

		// Fusioned weapon. Primary weapon slots.
		if (fusionedWeaponLevel == 1) {
			// Find all free slots in the primary weapon
			if (stoneCount >= targetItem.getSockets(false)) {
				AuditLogger.log(player, "Manastone socket overload");
				return false;
			}
		}
		// Fusioned weapon. Secondary weapon slots.
		else if (!targetItem.hasFusionedItem() || stoneCount >= targetItem.getSockets(true)) {
			// Find all free slots in the secondary weapon
			AuditLogger.log(player, "Manastone socket overload");
			return false;
		}

		// Start value of success
		float successChance = Rates.get(player, RatesConfig.MANASTONE_CHANCES);

		if (manastone.getItemTemplate().getItemQuality().getQualityId() >= ItemQuality.RARE.getQualityId())
			successChance *= 0.8f;

		// Next socket difficulty modifier
		float socketDiff = stoneCount * 1.25f + 1.75f;

		// Level difference
		successChance += (slotLevel - stoneLevel) / socketDiff;

		// The supplement item is used
		if (supplementItem != null) {
			int supplementUseCount = 0;
			ItemTemplate manastoneTemplate = manastone.getItemTemplate();

			int manastoneCount;
			// Not fusioned
			if (fusionedWeaponLevel == 1)
				manastoneCount = targetItem.getItemStones().size() + 1;
			// Fusioned
			else
				manastoneCount = targetItem.getFusionStones().size() + 1;

			// Additional success rate for the supplement
			ItemTemplate supplementTemplate = supplementItem.getItemTemplate();

			boolean isManastoneOnly = false;
			EnchantItemAction action = manastoneTemplate.getActions().getEnchantAction();
			if (action != null)
				supplementUseCount = action.getCount();

			action = supplementTemplate.getActions().getEnchantAction();
			if (action != null) {
				// Add successRate
				successChance += action.getChance();
				isManastoneOnly = action.isManastoneOnly();
			}

			if (isManastoneOnly)
				supplementUseCount = 1;
			else if (stoneCount > 0)
				supplementUseCount = supplementUseCount * manastoneCount;

			if (player.getInventory().getItemCountByItemId(supplementTemplate.getTemplateId()) < supplementUseCount)
				return false;

			// Put up supplements to wait for update
			player.subtractSupplements(supplementUseCount, supplementTemplate.getTemplateId());
		}

		boolean result = Rnd.chance() < successChance;

		// For test purpose. To use by administrator
		if (player.hasAccess(AdminConfig.ENCHANT_INFO))
			PacketSendUtility.sendMessage(player, (result ? "Success" : "Fail") + " (success chance:" + successChance + "%)");

		return result;
	}

	public static boolean socketManastoneAct(Player player, Item parentItem, Item targetItem, Item supplementItem, int targetWeapon, boolean result) {
		// Decrease required supplements
		player.updateSupplements();
		if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1))
			return false;
		if (result) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_SUCCEED(targetItem.getL10n()));

			ManaStone manaStone = ItemSocketService.addManaStone(targetItem, parentItem.getItemTemplate().getTemplateId(), targetWeapon != 1);
			if (targetItem.isEquipped()) {
				ItemEquipmentListener.addStoneStats(targetItem, manaStone, player.getGameStats());
				player.getGameStats().updateStatsAndSpeedVisually();
			}
		} else {
			targetItem.removeRemainingTuningCountIfPossible();
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_FAILED(targetItem.getL10n()));
		}

		ItemPacketService.updateItemAfterInfoChange(player, targetItem, ItemUpdateType.STATS_CHANGE);
		return true;
	}

	public static int getEquipBuff(Item item) {
		int[] skills;
		switch (item.getItemTemplate().getExceedEnchantSkill()) {
			case RANK1_SET1_MAGICAL_GLOVES:
				skills = new int[] { 13042, 13046, 13055 };
				break;
			case RANK1_SET1_MAGICAL_PANTS:
				skills = new int[] { 13071, 13075, 13078 };
				break;
			case RANK1_SET1_MAGICAL_SHOES:
				skills = new int[] { 13108, 13118, 13121 };
				break;
			case RANK1_SET1_MAGICAL_SHOULDER:
				skills = new int[] { 13104, 13097, 13098 };
				break;
			case RANK1_SET1_MAGICAL_TORSO:
				skills = new int[] { 13128, 13132, 13144 };
				break;
			case RANK1_SET1_MAGICAL_WEAPON:
				skills = new int[] { 13011, 13012, 13027 };
				break;
			case RANK1_SET1_PHYSICAL_GLOVES:
				skills = new int[] { 13042, 13046, 13055 };
				break;
			case RANK1_SET1_PHYSICAL_PANTS:
				skills = new int[] { 13071, 13075, 13078 };
				break;
			case RANK1_SET1_PHYSICAL_SHOES:
				skills = new int[] { 13108, 13118, 13121 };
				break;
			case RANK1_SET1_PHYSICAL_SHOULDER:
				skills = new int[] { 13104, 13097, 13098 };
				break;
			case RANK1_SET1_PHYSICAL_TORSO:
				skills = new int[] { 13128, 13132, 13144 };
				break;
			case RANK1_SET1_PHYSICAL_WEAPON:
				skills = new int[] { 13011, 13012, 13027 };
				break;
			case RANK1_SET2_MAGICAL_GLOVES:
				skills = new int[] { 13046, 13058, 13056 };
				break;
			case RANK1_SET2_MAGICAL_PANTS:
				skills = new int[] { 13075, 13061, 13067 };
				break;
			case RANK1_SET2_MAGICAL_SHOES:
				skills = new int[] { 13121, 13114, 13119 };
				break;
			case RANK1_SET2_MAGICAL_SHOULDER:
				skills = new int[] { 13104, 13094, 13192 };
				break;
			case RANK1_SET2_MAGICAL_TORSO:
				skills = new int[] { 13144, 13135, 13133 };
				break;
			case RANK1_SET2_MAGICAL_WEAPON:
				skills = new int[] { 13029, 13003, 13023 };
				break;
			case RANK1_SET2_PHYSICAL_GLOVES:
				skills = new int[] { 13046, 13058, 13056 };
				break;
			case RANK1_SET2_PHYSICAL_PANTS:
				skills = new int[] { 13075, 13064, 13069 };
				break;
			case RANK1_SET2_PHYSICAL_SHOES:
				skills = new int[] { 13121, 13114, 13119 };
				break;
			case RANK1_SET2_PHYSICAL_SHOULDER:
				skills = new int[] { 13104, 13094, 13192 };
				break;
			case RANK1_SET2_PHYSICAL_TORSO:
				skills = new int[] { 13144, 13135, 13133 };
				break;
			case RANK1_SET2_PHYSICAL_WEAPON:
				skills = new int[] { 13029, 13006, 13023 };
				break;
			case RANK1_SET3_MAGICAL_WEAPON:
				skills = new int[] { 13031, 13022, 13026 };
				break;
			case RANK1_SET3_PHYSICAL_WEAPON:
				skills = new int[] { 13031, 13022, 13026 };
				break;
			case RANK2_SET1_MAGICAL_GLOVES:
				skills = new int[] { 13050, 13047, 13057 };
				break;
			case RANK2_SET1_MAGICAL_PANTS:
				skills = new int[] { 13072, 13075, 13068 };
				break;
			case RANK2_SET1_MAGICAL_SHOES:
				skills = new int[] { 13125, 13122, 13120 };
				break;
			case RANK2_SET1_MAGICAL_SHOULDER:
				skills = new int[] { 13088, 13105, 13193 };
				break;
			case RANK2_SET1_MAGICAL_TORSO:
				skills = new int[] { 13139, 13145, 13134 };
				break;
			case RANK2_SET1_MAGICAL_WEAPON:
				skills = new int[] { 13008, 13010, 13024 };
				break;
			case RANK2_SET1_PHYSICAL_GLOVES:
				skills = new int[] { 13050, 13047, 13057 };
				break;
			case RANK2_SET1_PHYSICAL_PANTS:
				skills = new int[] { 13072, 13075, 13070 };
				break;
			case RANK2_SET1_PHYSICAL_SHOES:
				skills = new int[] { 13125, 13122, 13120 };
				break;
			case RANK2_SET1_PHYSICAL_SHOULDER:
				skills = new int[] { 13091, 13105, 13193 };
				break;
			case RANK2_SET1_PHYSICAL_TORSO:
				skills = new int[] { 13139, 13145, 13134 };
				break;
			case RANK2_SET2_MAGICAL_WEAPON:
				skills = new int[] { 13010, 13032, 13004 };
				break;
			case RANK2_SET2_PHYSICAL_GLOVES:
				skills = new int[] { 13050, 13043, 13059 };
				break;
			case RANK2_SET2_PHYSICAL_PANTS:
				skills = new int[] { 13072, 13078, 13065 };
				break;
			case RANK2_SET2_PHYSICAL_SHOES:
				skills = new int[] { 13125, 13109, 13115 };
				break;
			case RANK2_SET2_PHYSICAL_SHOULDER:
				skills = new int[] { 13091, 13099, 13095 };
				break;
			case RANK2_SET2_PHYSICAL_TORSO:
				skills = new int[] { 13139, 13129, 13136 };
				break;
			case RANK2_SET2_PHYSICAL_WEAPON:
				skills = new int[] { 13010, 13032, 13007 };
				break;
			case RANK2_SET1_PHYSICAL_WEAPON:
				skills = new int[] { 13008, 13010, 13024 };
				break;
			case RANK2_SET3_MAGICAL_WEAPON:
				skills = new int[] { 13008, 13013, 13030 };
				break;
			case RANK2_SET3_PHYSICAL_WEAPON:
				skills = new int[] { 13008, 13013, 13030 };
				break;
			case RANK3_SET1_MAGICAL_GLOVES:
				skills = new int[] { 13038, 13051, 13060 };
				break;
			case RANK3_SET1_MAGICAL_PANTS:
				skills = new int[] { 13080, 13073, 13063 };
				break;
			case RANK3_SET1_MAGICAL_SHOES:
				skills = new int[] { 13112, 13126, 13116 };
				break;
			case RANK3_SET1_MAGICAL_SHOULDER:
				skills = new int[] { 13082, 13089, 13096 };
				break;
			case RANK3_SET1_MAGICAL_TORSO:
				skills = new int[] { 13142, 13140, 13137 };
				break;
			case RANK3_SET1_MAGICAL_WEAPON:
				skills = new int[] { 13034, 13018, 13009 };
				break;
			case RANK3_SET1_PHYSICAL_GLOVES:
				skills = new int[] { 13040, 13051, 13060 };
				break;
			case RANK3_SET1_PHYSICAL_PANTS:
				skills = new int[] { 13080, 13073, 13066 };
				break;
			case RANK3_SET1_PHYSICAL_SHOES:
				skills = new int[] { 13112, 13126, 13116 };
				break;
			case RANK3_SET1_PHYSICAL_SHOULDER:
				skills = new int[] { 13084, 13092, 13096 };
				break;
			case RANK3_SET1_PHYSICAL_TORSO:
				skills = new int[] { 13142, 13140, 13137 };
				break;
			case RANK3_SET1_PHYSICAL_WEAPON:
				skills = new int[] { 13036, 13020, 13009 };
				break;
			case RANK3_SET2_MAGICAL_GLOVES:
				skills = new int[] { 13038, 13048, 13044 };
				break;
			case RANK3_SET2_MAGICAL_PANTS:
				skills = new int[] { 13080, 13076, 13079 };
				break;
			case RANK3_SET2_MAGICAL_SHOES:
				skills = new int[] { 13112, 13123, 13110 };
				break;
			case RANK3_SET2_MAGICAL_SHOULDER:
				skills = new int[] { 13082, 13106, 13100 };
				break;
			case RANK3_SET2_MAGICAL_TORSO:
				skills = new int[] { 13142, 13146, 13130 };
				break;
			case RANK3_SET2_MAGICAL_WEAPON:
				skills = new int[] { 13034, 13014, 13025 };
				break;
			case RANK3_SET2_PHYSICAL_GLOVES:
				skills = new int[] { 13040, 13048, 13044 };
				break;
			case RANK3_SET2_PHYSICAL_PANTS:
				skills = new int[] { 13080, 13076, 13079 };
				break;
			case RANK3_SET2_PHYSICAL_SHOES:
				skills = new int[] { 13112, 13123, 13110 };
				break;
			case RANK3_SET2_PHYSICAL_SHOULDER:
				skills = new int[] { 13084, 13106, 13100 };
				break;
			case RANK3_SET2_PHYSICAL_TORSO:
				skills = new int[] { 13142, 13146, 13130 };
				break;
			case RANK3_SET2_PHYSICAL_WEAPON:
				skills = new int[] { 13036, 13014, 13025 };
				break;
			case RANK3_SET3_MAGICAL_WEAPON:
				skills = new int[] { 13018, 13014, 13033 };
				break;
			case RANK3_SET3_PHYSICAL_WEAPON:
				skills = new int[] { 13020, 13014, 13033 };
				break;
			case RANK2_SET2_MAGICAL_GLOVES:
				skills = new int[] { 13050, 13043, 13059 };
				break;
			case RANK2_SET2_MAGICAL_PANTS:
				skills = new int[] { 13072, 13078, 13062 };
				break;
			case RANK2_SET2_MAGICAL_SHOES:
				skills = new int[] { 13125, 13109, 13115 };
				break;
			case RANK2_SET2_MAGICAL_SHOULDER:
				skills = new int[] { 13088, 13099, 13095 };
				break;
			case RANK2_SET2_MAGICAL_TORSO:
				skills = new int[] { 13139, 13129, 13136 };
				break;
			case RANK4_SET1_MAGICAL_GLOVES:
				skills = new int[] { 13053, 13039, 13045 };
				break;
			case RANK4_SET1_MAGICAL_PANTS:
				skills = new int[] { 13077, 13081, 13079 };
				break;
			case RANK4_SET1_MAGICAL_SHOES:
				skills = new int[] { 13117, 13113, 13111 };
				break;
			case RANK4_SET1_MAGICAL_SHOULDER:
				skills = new int[] { 13086, 13083, 13101 };
				break;
			case RANK4_SET1_MAGICAL_TORSO:
				skills = new int[] { 13138, 13143, 13131 };
				break;
			case RANK4_SET1_MAGICAL_WEAPON:
				skills = new int[] { 13015, 13028, 13017 };
				break;
			case RANK4_SET1_PHYSICAL_GLOVES:
				skills = new int[] { 13054, 13041, 13045 };
				break;
			case RANK4_SET1_PHYSICAL_PANTS:
				skills = new int[] { 13077, 13081, 13079 };
				break;
			case RANK4_SET1_PHYSICAL_SHOES:
				skills = new int[] { 13117, 13113, 13111 };
				break;
			case RANK4_SET1_PHYSICAL_SHOULDER:
				skills = new int[] { 13087, 13085, 13101 };
				break;
			case RANK4_SET1_PHYSICAL_TORSO:
				skills = new int[] { 13138, 13143, 13131 };
				break;
			case RANK4_SET1_PHYSICAL_WEAPON:
				skills = new int[] { 13016, 13028, 13017 };
				break;
			case RANK4_SET2_MAGICAL_GLOVES:
				skills = new int[] { 13053, 13052, 13049 };
				break;
			case RANK4_SET2_MAGICAL_PANTS:
				skills = new int[] { 13077, 13074, 13076 };
				break;
			case RANK4_SET2_MAGICAL_SHOES:
				skills = new int[] { 13117, 13127, 13124 };
				break;
			case RANK4_SET2_MAGICAL_SHOULDER:
				skills = new int[] { 13086, 13090, 13107 };
				break;
			case RANK4_SET2_MAGICAL_TORSO:
				skills = new int[] { 13138, 13141, 13147 };
				break;
			case RANK4_SET2_MAGICAL_WEAPON:
				skills = new int[] { 13019, 13017, 13005 };
				break;
			case RANK4_SET2_PHYSICAL_GLOVES:
				skills = new int[] { 13054, 13052, 13049 };
				break;
			case RANK4_SET2_PHYSICAL_PANTS:
				skills = new int[] { 13077, 13074, 13076 };
				break;
			case RANK4_SET2_PHYSICAL_SHOES:
				skills = new int[] { 13117, 13127, 13124 };
				break;
			case RANK4_SET2_PHYSICAL_SHOULDER:
				skills = new int[] { 13087, 13093, 13107 };
				break;
			case RANK4_SET2_PHYSICAL_TORSO:
				skills = new int[] { 13138, 13141, 13147 };
				break;
			case RANK4_SET2_PHYSICAL_WEAPON:
				skills = new int[] { 13021, 13017, 13005 };
				break;
			case RANK4_SET3_MAGICAL_WEAPON:
				skills = new int[] { 13035, 13001, 13005 };
				break;
			case RANK4_SET3_PHYSICAL_WEAPON:
				skills = new int[] { 13037, 13001, 13005 };
				break;
			case RANK5_SET1_MAGICAL_TORSO:
				skills = new int[] { 13235, 13236, 13238 };
				break;
			case RANK5_SET1_MAGICAL_GLOVES:
				skills = new int[] { 13248, 13253, 13251 };
				break;
			case RANK5_SET1_MAGICAL_PANTS:
				skills = new int[] { 13241, 13079, 13240 };
				break;
			case RANK5_SET1_MAGICAL_SHOULDER:
				skills = new int[] { 13269, 13279, 13247 };
				break;
			case RANK5_SET1_MAGICAL_SHOES:
				skills = new int[] { 13245, 13266, 13246 };
				break;
			case RANK5_SET1_PHYSICAL_TORSO:
				skills = new int[] { 13235, 13236, 13238 };
				break;
			case RANK5_SET1_PHYSICAL_GLOVES:
				skills = new int[] { 13251, 13249, 13248 };
				break;
			case RANK5_SET1_PHYSICAL_SHOULDER:
				skills = new int[] { 13270, 13247, 13269 };
				break;
			case RANK5_SET1_PHYSICAL_PANTS:
				skills = new int[] { 13241, 13079, 13240 };
				break;
			case RANK5_SET1_PHYSICAL_SHOES:
				skills = new int[] { 13245, 13244, 13266 };
				break;
			case RANK5_SET1_MAGICAL_WEAPON:
				skills = new int[] { 13228, 13234, 13231 };
				break;
			case RANK5_SET1_PHYSICAL_WEAPON:
				skills = new int[] { 13229, 13234, 13231 };
				break;
			default:
				throw new UnsupportedOperationException("Unhandled breakthrough skill type " + item.getItemTemplate().getExceedEnchantSkill());
		}
		return Rnd.get(skills);
	}

	public static void amplifyItem(Player player, int targetItemObjId, int materialId, int toolId) {
		if (player == null)
			return;
		Item targetItem = player.getEquipment().getEquippedItemByObjId(targetItemObjId);

		if (targetItem == null)
			targetItem = player.getInventory().getItemByObjId(targetItemObjId);

		Item material = player.getInventory().getItemByObjId(materialId);
		Item tool = player.getInventory().getItemByObjId(toolId);

		if (targetItem == null || material == null || tool == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_NO_TARGET_ITEM());
			return;
		}
		if (targetItem.isAmplified()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_ALREADY());
			return;
		}
		if (!targetItem.getItemTemplate().canExceedEnchant()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_CANNOT_01(targetItem.getL10n()));
			return;
		}
		if (targetItem.getEnchantLevel() < targetItem.getMaxEnchantLevel()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_CANNOT_02());
			return;
		}
		if (targetItem.getItemId() != material.getItemId() && material.getItemId() != 166500002 && material.getItemId() != 166500005) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_NO_TARGET_ITEM());
			return;
		}
		if (player.getInventory().decreaseByObjectId(material.getObjectId(), 1) && player.getInventory().decreaseByObjectId(tool.getObjectId(), 1)) {
			targetItem.setAmplified(true);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_SUCCEED(targetItem.getL10n()));
			ItemPacketService.updateItemAfterInfoChange(player, targetItem);

			if (targetItem.isEquipped())
				player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
			else
				player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
	}
}
