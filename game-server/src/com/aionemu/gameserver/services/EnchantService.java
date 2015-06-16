package com.aionemu.gameserver.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.EnchantsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.enchants.EnchantEffect;
import com.aionemu.gameserver.model.enchants.EnchantStat;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.stats.listeners.ItemEquipmentListener;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.EnchantItemAction;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemSocketService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 * @modified Wakizashi, Source, vlog
 */
public class EnchantService {

	// private static final Logger log = LoggerFactory.getLogger(EnchantService.class);

	/**
	 * @param player
	 * @param targetItem
	 * @param parentItem
	 */
	public static boolean breakItem(Player player, Item targetItem, Item parentItem) {
		Storage inventory = player.getInventory();

		if (inventory.getItemByObjId(targetItem.getObjectId()) == null)
			return false;
		if (inventory.getItemByObjId(parentItem.getObjectId()) == null)
			return false;

		ItemTemplate itemTemplate = targetItem.getItemTemplate();
		int quality = itemTemplate.getItemQuality().getQualityId();

		if (!itemTemplate.isArmor() && !itemTemplate.isWeapon()) {
			AuditLogger.info(player, "Player try break dont compatible item type.");
			return false;
		}

		if (!itemTemplate.isArmor() && !itemTemplate.isWeapon()) {
			AuditLogger.info(player, "Break item hack, armor/weapon iD changed.");
			return false;
		}

		// Quality modifier
		if (itemTemplate.isSoulBound() && !itemTemplate.isArmor())
			quality += 1;
		else if (!itemTemplate.isSoulBound() && itemTemplate.isArmor())
			quality -= 1;

		int number = 0;
		int level = 1;
		switch (quality) {
			case 0: // JUNK
			case 1: // COMMON
				number = Rnd.get(1, 2);
				level = Rnd.get(-4, 10);
				break;
			case 2: // RARE
				number = Rnd.get(1, 4);
				level = Rnd.get(-3, 20);
				break;
			case 3: // LEGEND
				number = Rnd.get(1, 6);
				level = Rnd.get(-2, 30);
				break;
			case 4: // UNIQUE
				number = Rnd.get(1, 8);
				level = Rnd.get(-1, 50);
				break;
			case 5: // EPIC
				number = Rnd.get(1, 10);
				level = Rnd.get(0, 70);
				break;
			case 6: // MYTHIC
			case 7:
				number = Rnd.get(1, 12);
				level = Rnd.get(0, 80);
				break;
		}

		// You can't add stone < 166000000
		if (level < 1)
			level = 1;
		int enchantItemLevel = targetItem.getItemTemplate().getLevel() + level;
		int enchantItemId = 166000000 + enchantItemLevel;

		if (inventory.delete(targetItem) != null) {
			if (inventory.decreaseByObjectId(parentItem.getObjectId(), 1))
				ItemService.addItem(player, enchantItemId, number);
		}
		else
			AuditLogger.info(player, "Possible break item hack, do not remove item.");
		return true;
	}

	/**
	 * @param player
	 * @param parentItem
	 *          the enchantment stone
	 * @param targetItem
	 *          the item to enchant
	 * @param supplementItem
	 *          the item, giving additional chance
	 * @return true, if successful
	 */
	public static boolean enchantItem(Player player, Item parentItem, Item targetItem, Item supplementItem) {
		ItemTemplate enchantStone = parentItem.getItemTemplate();
		int enchantStoneLevel = enchantStone.getLevel();
		int targetItemLevel = targetItem.getItemTemplate().getLevel();
		int enchantitemLevel = targetItem.getEnchantLevel() + 1;

		// Modifier, depending on the quality of the item
		// Decreases the chance of enchant
		int qualityCap = 0;

		ItemQuality quality = targetItem.getItemTemplate().getItemQuality();

		switch (quality) {
			case JUNK:
			case COMMON:
				qualityCap = 5;
				break;
			case RARE:
				qualityCap = 10;
				break;
			case LEGEND:
				qualityCap = 15;
				break;
			case UNIQUE:
				qualityCap = 20;
				break;
			case EPIC:
				qualityCap = 25;
				break;
			case MYTHIC:
				qualityCap = 30;
				break;
		}

		// Start value of success
		float success = EnchantsConfig.ENCHANT_STONE;

		// Extra success chance
		// The greater the enchant stone level, the greater the
		// level difference modifier
		int levelDiff = enchantStoneLevel - targetItemLevel;
		success += levelDiff > 0 ? levelDiff * 3f / qualityCap : 0;

		// Level difference
		// Can be negative, if the item quality too hight
		// And the level difference too small
		success += levelDiff - qualityCap;

		// Enchant next level difficulty
		// The greater item enchant level,
		// the lower start success chance
		success -= targetItem.getEnchantLevel() * qualityCap / (enchantitemLevel > 10 ? 4f : 5f);

		// Supplement is used
		if (supplementItem != null) {
			// Amount of supplement items
			int supplementUseCount = 1;
			// Additional success rate for the supplement
			ItemTemplate supplementTemplate = supplementItem.getItemTemplate();
			float addSuccessRate = 0f;

			EnchantItemAction action = supplementTemplate.getActions().getEnchantAction();
			if (action != null) {
				if (action.isManastoneOnly())
					return false;
				addSuccessRate = action.getChance() * 2;
			}

			action = enchantStone.getActions().getEnchantAction();
			if (action != null)
				supplementUseCount = action.getCount();

			// Beginning from the level 11 of the enchantment of the item,
			// There will be 2 times more supplements required
			if (enchantitemLevel > 10)
				supplementUseCount = supplementUseCount * 2;

			// Check the required amount of the supplements
			if (player.getInventory().getItemCountByItemId(supplementTemplate.getTemplateId()) < supplementUseCount)
				return false;

			// Adjust addsuccessrate to rates in config
			switch (parentItem.getItemTemplate().getItemQuality()) {
				case LEGEND:
					addSuccessRate *= EnchantsConfig.LESSER_SUP;
					break;
				case UNIQUE:
					addSuccessRate *= EnchantsConfig.REGULAR_SUP;
					break;
				case EPIC:
					addSuccessRate *= EnchantsConfig.GREATER_SUP;
					break;
				case MYTHIC:
					addSuccessRate *= EnchantsConfig.MYTHIC_SUP;
					break;
			}

			// Add success rate of the supplement to the overall chance
			success += addSuccessRate;

			// Put supplements to wait for update
			player.subtractSupplements(supplementUseCount, supplementTemplate.getTemplateId());
		}

		// If item is amplified we need to put some more chance to it
		if (targetItem.isAmplified()) {
			success += 120 -targetItem.getEnchantLevel() * 1.0f;
		}

		// The overall success chance can't be more, than 95
		if (success >= 95)
			success = 95;

		boolean result = false;
		float random = Rnd.get(1, 1000) / 10f;

		// If the random number < or = overall success rate,
		// The item will be successfully enchanted
		
		if (random <= success)
			result = true;

		// For test purpose. To use by administrator
		if (player.getAccessLevel() > 2)
			PacketSendUtility.sendMessage(player, (result ? "Success" : "Fail") + " Rnd:" + random + " Luck:" + success);

		return result;
	}

	public static void enchantItemAct(Player player, Item parentItem, Item targetItem, Item supplementItem, int currentEnchant, boolean result) {
		int addLevel = 1;
		// TODO: what it is supposed to do and when ? 
		int rnd = Rnd.get(100); // crit modifier
		int maxEnchant = targetItem.getItemTemplate().getMaxEnchantLevel(); // max enchant level from item_templates
		maxEnchant += targetItem.getEnchantBonus();
		if (rnd < 2)
			addLevel = 3;
		else if (rnd < 7)
			addLevel = 2;

		if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
			AuditLogger.info(player, "Possible enchant hack, do not remove enchant stone.");
			return;
		}
		// Decrease required supplements
		player.updateSupplements();

		// Items that are Fabled or Eternal can get up to +15.
		if (result) {
			if (currentEnchant + addLevel <= maxEnchant) {
				currentEnchant += addLevel;
			}else if (targetItem.isAmplified() && parentItem.getItemId() == 166020000) { //Omega Enchantment Stone
				currentEnchant += 1;
			}else if (((addLevel - 1) > 1) && ((currentEnchant + addLevel - 1) <= maxEnchant)) {
				currentEnchant += (addLevel - 1);
			}
			else
				currentEnchant += 1;
		}
		else {
			// Retail: http://powerwiki.na.aiononline.com/aion/Patch+Notes:+1.9.0.1
			// When socketing fails at +11~+15, the value falls back to +10.
			if (targetItem.isAmplified()) {
				currentEnchant = maxEnchant;
				targetItem.setAmplified(false);
				if (targetItem.getBuffSkill() != 0) {
					SkillLearnService.removeSkill(player, targetItem.getBuffSkill());
					targetItem.setBuffSkill(0);
				}
			} else if ((currentEnchant > 10 && maxEnchant > 10) && !targetItem.isAmplified()) {
				currentEnchant = 10;
			} else if (currentEnchant > 0 && !targetItem.isAmplified()) {
				currentEnchant -= 1;
			}
		}

		if(targetItem.isAmplified()) {
			maxEnchant = EnchantsConfig.MAX_AMPLIFICATION_LEVEL;
		}

		// Temp fix for not increasing over max; see TODO above
		targetItem.setEnchantLevel(Math.min(currentEnchant, maxEnchant));
	
		if (targetItem.getEnchantEffect() != null) {
			targetItem.getEnchantEffect().endEffect(player);
			targetItem.setEnchantEffect(null);
		}
		
		int buffId  = 0;
		if (targetItem.getEnchantLevel() == 20) {
			if (targetItem.getItemTemplate().isArmor() && targetItem.getItemTemplate().getItemGroup() != ItemGroup.SHIELD) {
				buffId = getArmorBuff(player, targetItem);
			} else if (targetItem.getItemTemplate().isWeapon() || targetItem.getItemTemplate().getItemGroup() == ItemGroup.SHIELD) {
				buffId = getWeaponBuff(player, targetItem);
			}
			targetItem.setBuffSkill(buffId);
			//targetItem.setPackCount(targetItem.getPackCount() + 1);
			SkillLearnService.addSkill(player, buffId);
		}
		
		if (targetItem.isEquipped())
			player.getGameStats().updateStatsVisually();

		ItemPacketService.updateItemAfterInfoChange(player, targetItem, ItemUpdateType.STATS_CHANGE);
		
		
		if (targetItem.getEnchantLevel() > 0 && targetItem.isEquipped()) {
			ItemGroup itemGroup = targetItem.getItemTemplate().getItemGroup();
			HashMap<Integer, List<EnchantStat>> enchant = DataManager.ENCHANT_DATA.getTemplates(itemGroup);
			if (enchant != null) {
				targetItem.setEnchantEffect(new EnchantEffect(targetItem, player, enchant.get(targetItem.getEnchantLevel())));
			}
		}
		
		if (result) 
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENCHANT_ITEM_SUCCEED_NEW(new DescriptionId(targetItem.getNameId()), addLevel));
		else { 
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_FAILED(new DescriptionId(targetItem.getNameId())));
			if (targetItem.getItemTemplate().getEnchantType() > 0) {
				if (targetItem.isEquipped())
					player.getEquipment().decreaseEquippedItemCount(targetItem.getObjectId(), 1);
				else
					player.getInventory().decreaseByObjectId(targetItem.getObjectId(), 1);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENCHANT_TYPE1_ENCHANT_FAIL(new DescriptionId(targetItem.getNameId())));
			}
		}
		if (targetItem.getPersistentState() != PersistentState.DELETED) {
			if (targetItem.isEquipped())
				player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
			else
				player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
	}

	/**
	 * @param player
	 * @param parentItem
	 *          the manastone
	 * @param targetItem
	 *          the item to socket
	 * @param supplementItem
	 * @param targetWeapon
	 *          fusioned weapon
	 */
	public static boolean socketManastone(Player player, Item parentItem, Item targetItem, Item supplementItem, int targetWeapon) {

		int targetItemLevel = 1;

		// Fusioned weapon. Primary weapon level.
		if (targetWeapon == 1)
			targetItemLevel = targetItem.getItemTemplate().getLevel();
		// Fusioned weapon. Secondary weapon level.
		else
			targetItemLevel = targetItem.getFusionedItemTemplate().getLevel();

		int stoneLevel = parentItem.getItemTemplate().getLevel();
		int slotLevel = (int) (10 * Math.ceil((targetItemLevel + 10) / 10d));
		boolean result = false;

		// Start value of success
		float success = EnchantsConfig.MANA_STONE;

		// The current amount of socketed stones
		int stoneCount;

		// Manastone level shouldn't be greater as 20 + item level
		// Example: item level: 1 - 10. Manastone level should be <= 20
		if (stoneLevel > slotLevel)
			return false;

		// Fusioned weapon. Primary weapon slots.
		if (targetWeapon == 1)
			// Count the inserted stones in the primary weapon
			stoneCount = targetItem.getItemStones().size();
		// Fusioned weapon. Secondary weapon slots.
		else
			// Count the inserted stones in the secondary weapon
			stoneCount = targetItem.getFusionStones().size();

		// Fusioned weapon. Primary weapon slots.
		if (targetWeapon == 1) {
			// Find all free slots in the primary weapon
			if (stoneCount >= targetItem.getSockets(false)) {
				AuditLogger.info(player, "Manastone socket overload");
				return false;
			}
		}
		// Fusioned weapon. Secondary weapon slots.
		else if (!targetItem.hasFusionedItem() || stoneCount >= targetItem.getSockets(true)) {
			// Find all free slots in the secondary weapon
			AuditLogger.info(player, "Manastone socket overload");
			return false;
		}

		// Stone quality modifier
		success += parentItem.getItemTemplate().getItemQuality() == ItemQuality.COMMON ? 25f : 15f;

		// Next socket difficulty modifier
		float socketDiff = stoneCount * 1.25f + 1.75f;

		// Level difference
		success += (slotLevel - stoneLevel) / socketDiff;

		// The supplement item is used
		if (supplementItem != null) {
			int supplementUseCount = 0;
			ItemTemplate manastoneTemplate = parentItem.getItemTemplate();

			int manastoneCount;
			// Not fusioned
			if (targetWeapon == 1)
				manastoneCount = targetItem.getItemStones().size() + 1;
			// Fusioned
			else
				manastoneCount = targetItem.getFusionStones().size() + 1;

			// Additional success rate for the supplement
			ItemTemplate supplementTemplate = supplementItem.getItemTemplate();
			float addSuccessRate = 0f;

			boolean isManastoneOnly = false;
			EnchantItemAction action = manastoneTemplate.getActions().getEnchantAction();
			if (action != null)
				supplementUseCount = action.getCount();

			action = supplementTemplate.getActions().getEnchantAction();
			if (action != null) {
				addSuccessRate = action.getChance();
				isManastoneOnly = action.isManastoneOnly();
			}

			// Adjust addsuccessrate to rates in config
			switch (parentItem.getItemTemplate().getItemQuality()) {
				case LEGEND:
					addSuccessRate *= EnchantsConfig.LESSER_SUP;
					break;
				case UNIQUE:
					addSuccessRate *= EnchantsConfig.REGULAR_SUP;
					break;
				case EPIC:
					addSuccessRate *= EnchantsConfig.GREATER_SUP;
					break;
			}

			if (isManastoneOnly)
				supplementUseCount = 1;
			else if (stoneCount > 0)
				supplementUseCount = supplementUseCount * manastoneCount;

			if (player.getInventory().getItemCountByItemId(supplementTemplate.getTemplateId()) < supplementUseCount)
				return false;

			// Add successRate
			success += addSuccessRate;

			// Put up supplements to wait for update
			player.subtractSupplements(supplementUseCount, supplementTemplate.getTemplateId());
		}

		float random = Rnd.get(1, 1000) / 10f;

		if (random <= success)
			result = true;

		// For test purpose. To use by administrator
		if (player.getAccessLevel() > 2)
			PacketSendUtility.sendMessage(player, (result ? "Success" : "Fail") + " Rnd:" + random + " Luck:" + success);

		return result;
	}

	public static void socketManastoneAct(Player player, Item parentItem, Item targetItem, Item supplementItem, int targetWeapon,
		boolean result) {

		// Decrease required supplements
		player.updateSupplements();

		if (player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1) && result) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_SUCCEED(new DescriptionId(targetItem.getNameId())));

			if (targetWeapon == 1) {
				ManaStone manaStone = ItemSocketService.addManaStone(targetItem, parentItem.getItemTemplate().getTemplateId());
				if (targetItem.isEquipped()) {
					ItemEquipmentListener.addStoneStats(targetItem, manaStone, player.getGameStats());
					player.getGameStats().updateStatsAndSpeedVisually();
				}
			}
			else {
				ManaStone manaStone = ItemSocketService.addFusionStone(targetItem, parentItem.getItemTemplate().getTemplateId());
				if (targetItem.isEquipped()) {
					ItemEquipmentListener.addStoneStats(targetItem, manaStone, player.getGameStats());
					player.getGameStats().updateStatsAndSpeedVisually();
				}
			}
		}
		else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_FAILED(new DescriptionId(targetItem.getNameId())));
			player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1);
		}
		
		ItemPacketService.updateItemAfterInfoChange(player, targetItem, ItemUpdateType.STATS_CHANGE);
	}

	public static int getWeaponBuff(Player player, Item item) {
		List<Integer> skillId = new ArrayList<Integer>();
		for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplates()) {
			if (st.getStack().startsWith("WS_"))
				skillId.add(st.getSkillId());
		}
		return skillId.get(Rnd.get(0, skillId.size() - 1));
	}

	public static int getArmorBuff(Player player, Item item) {
		List<Integer> skillId = new ArrayList<Integer>();
		switch (item.getItemTemplate().getItemGroup()) {
			case PL_TORSO:
			case CH_TORSO:
			case LT_TORSO:
			case RB_TORSO:
			{
				for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplates()) {
					if (st.getStack().startsWith("AT_") && st.isPassive())
						skillId.add(st.getSkillId());
				}
			}
			case PL_SHOES:
			case CH_SHOES:
			case LT_SHOES:
			case RB_SHOES:
			{
				for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplates()) {
					if (st.getStack().startsWith("ASO_") && st.isPassive())
						skillId.add(st.getSkillId());
				}
			}
			case PL_PANTS:
			case CH_PANTS:
			case LT_PANTS:
			case RB_PANTS:
			{
				for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplates()) {
					if (st.getStack().startsWith("AP_") && st.isPassive())
						skillId.add(st.getSkillId());
				}
			}
			case PL_GLOVES:
			case CH_GLOVES:
			case LT_GLOVES:
			case RB_GLOVES:
			{
				for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplates()) {
					if (st.getStack().startsWith("AG_") && st.isPassive())
						skillId.add(st.getSkillId());
				}
			}
			case PL_SHOULDER:
			case CH_SHOULDER:
			case LT_SHOULDER:
			case RB_SHOULDER:
			{
				for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplates()) {
					if (st.getStack().startsWith("ASD_") && st.isPassive())
						skillId.add(st.getSkillId());
				}
			}
		}
		return skillId.get(Rnd.get(0, skillId.size() - 1));
	}

	public static void amplifyItem(Player player, int targetItemObjId, int materialId, int toolId) {
	    
		if (player == null) {
			return;
		}

		Item targetItem = player.getEquipment().getEquippedItemByObjId(targetItemObjId);

		if (targetItem == null) {
			targetItem = player.getInventory().getItemByObjId(targetItemObjId);
		}

		Item material = player.getInventory().getItemByObjId(materialId);
		Item tool = player.getInventory().getItemByObjId(toolId);

		if (targetItem == null || material == null || tool == null) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402655));
			return;
		}

		if (targetItem.isAmplified()) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402656));
			return;
		}

		if (!targetItem.getItemTemplate().canExceedEnchant()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_CANNOT_01(new DescriptionId(targetItem.getNameId())));
			return;
		}

		if (targetItem.getEnchantLevel() < targetItem.getMaxEnchantLevel()) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402651));
			return;
		}

		if (targetItem.getItemId() != material.getItemId() && material.getItemId() != 166500002) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402655));
			return;
		}

		if (player.getInventory().decreaseByObjectId(material.getObjectId(), 1) &&
			player.getInventory().decreaseByObjectId(tool.getObjectId(), 1)) {
			targetItem.setAmplified(true);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_SUCCEED(new DescriptionId(targetItem.getNameId())));
			ItemPacketService.updateItemAfterInfoChange(player, targetItem);
			
			if (targetItem.isEquipped())
				player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
			else
				player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
	}
}
