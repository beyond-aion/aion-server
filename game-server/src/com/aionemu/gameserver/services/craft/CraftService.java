package com.aionemu.gameserver.services.craft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.StaticObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RewardType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.recipe.Component;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CRAFT_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CRAFT_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.skillengine.task.CraftingTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author MrPoke, sphinx, synchro2, Evil_dnk
 */
public class CraftService {

	private static final Logger log = LoggerFactory.getLogger("CRAFT_LOG");

	/**
	 * @param player
	 * @param recipetemplate
	 * @param critCount
	 */
	public static void finishCrafting(final Player player, RecipeTemplate recipetemplate, int critCount, int bonus) {

		if (recipetemplate.getMaxProductionCount() != null) {
			player.getRecipeList().deleteRecipe(player, recipetemplate.getId());
			if (critCount == 0) {
				QuestEngine.getInstance().onFailCraft(new QuestEnv(null, player, 0, 0), recipetemplate.getComboProduct(1) == null? 0 : recipetemplate.getComboProduct(1));
			}
		}

		int xpReward = (int) ((0.008 * (recipetemplate.getSkillpoint() + 100) * (recipetemplate.getSkillpoint() + 100) + 60));
		xpReward = xpReward + (xpReward * bonus / 100); // bonus
		int productItemId = critCount > 0 ? recipetemplate.getComboProduct(critCount) : recipetemplate.getProductid();

		ItemService.addItem(player, productItemId, recipetemplate.getQuantity(), new ItemUpdatePredicate() {

			@Override
			public boolean changeItem(Item item) {
				if (item.getItemTemplate().isWeapon() || item.getItemTemplate().isArmor()) {
					item.setItemCreator(player.getName());
				}
				return true;
			}
		});

		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(productItemId);
		if (LoggingConfig.LOG_CRAFT) {
			log.info((critCount > 0 ? "[CRAFT][Critical] ID/Count" : "[CRAFT][Normal] ID/Count") + (LoggingConfig.ENABLE_ADVANCED_LOGGING ? "/Item Name - " + productItemId + "/" + recipetemplate.getQuantity() + "/" + itemTemplate.getName() : " - " + productItemId + "/" + recipetemplate.getQuantity()) + 
			" to player " + player.getName());
		}

		int gainedCraftExp = (int) RewardType.CRAFTING.calcReward(player, xpReward);

		if (player.getSkillList().addSkillXp(player, recipetemplate.getSkillid(), gainedCraftExp, recipetemplate.getSkillpoint())) {
			player.getCommonData().addExp(xpReward, RewardType.CRAFTING);
		}
		else {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_MSG_DONT_GET_PRODUCTION_EXP(DataManager.SKILL_DATA.getSkillTemplate(recipetemplate.getSkillid()).getNameId()));
		}

		if (recipetemplate.getCraftDelayId() != null) {
			player.getCraftCooldownList().addCraftCooldown(recipetemplate.getCraftDelayId(),
				recipetemplate.getCraftDelayTime());
		}
	}

	/**
	 * @param player
	 * @param targetTemplateId
	 * @param recipeId
	 * @param targetObjId
	 */
	public static void startCrafting(Player player, int recipeId, int targetObjId, int craftType) {

		RecipeTemplate recipeTemplate = DataManager.RECIPE_DATA.getRecipeTemplateById(recipeId);
		int skillId = recipeTemplate.getSkillid();
		VisibleObject target = player.getKnownList().getObject(targetObjId);
		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getProductid());

		if (!checkCraft(player, recipeTemplate, skillId, target, itemTemplate, craftType)) {
			sendCancelCraft(player, skillId, targetObjId, itemTemplate);
			return;
		}

		if (recipeTemplate.getDp() != null)
			player.getCommonData().addDp(-recipeTemplate.getDp());

		int skillLvlDiff = player.getSkillList().getSkillLevel(skillId) - recipeTemplate.getSkillpoint();
		player.setCraftingTask(new CraftingTask(player, (StaticObject) target, recipeTemplate, skillLvlDiff, craftType == 1 ? 15 : 0));

		if(skillId == 40009)
			player.getCraftingTask().setInterval(200);

		player.getCraftingTask().start();
	}

	private static boolean checkCraft(Player player, RecipeTemplate recipeTemplate, int skillId, VisibleObject target,
		ItemTemplate itemTemplate, int craftType) {

		if (recipeTemplate == null) {
			return false;
		}

		if (itemTemplate == null) {
			return false;
		}

		if (player.getCraftingTask() != null && player.getCraftingTask().isInProgress()) {
			return false;
		}

		// morphing dont need static object/npc to use
		if ((skillId != 40009) && (target == null || !(target instanceof StaticObject))) {
			AuditLogger.info(player, " tried to craft incorrect target.");
			return false;
		}

		if (recipeTemplate.getDp() != null && (player.getCommonData().getDp() < recipeTemplate.getDp())) {
			AuditLogger.info(player, " try craft without required DP count.");
			return false;
		}

		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_INVENTORY_IS_FULL);
			return false;
		}
		
		if (!player.getRecipeList().isRecipePresent(recipeTemplate.getId())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_CAN_NOT_FIND_RECIPE);
			return false;
		}
		
		if (recipeTemplate.getCraftDelayId() != null) {
			if (!player.getCraftCooldownList().isCanCraft(recipeTemplate.getCraftDelayId())) {
				AuditLogger.info(player, " try craft item before cooldown expire.");
				return false;
			}
		}

		if (!player.getSkillList().isSkillPresent(skillId)
			|| player.getSkillList().getSkillLevel(skillId) < recipeTemplate.getSkillpoint()) {
			AuditLogger.info(player, " tried craft without required skill.");
			return false;
		}
		
		if (craftType == 1 && !player.getInventory().decreaseByItemId(getBonusReqItem(skillId), 1)) {
			AuditLogger.info(player, " tried craft without 169401079.");
			return false;
		}
		
		for (Component component : recipeTemplate.getComponent()) {
			if (!player.getInventory().decreaseByItemId(component.getItemid(), component.getQuantity())) {
				AuditLogger.info(player, " tried craft without required items.");
				return false;
			}
		}

		return true;
	}

	private static void sendCancelCraft(Player player, int skillId, int targetObjId, ItemTemplate itemTemplate) {

		PacketSendUtility.sendPacket(player, new SM_CRAFT_UPDATE(skillId, itemTemplate, 0, 0, 4));
		PacketSendUtility.broadcastPacket(player, new SM_CRAFT_ANIMATION(player.getObjectId(), targetObjId, 0, 2), true);
	}

	private static int getBonusReqItem(int skillId) {
		switch (skillId) {
			case 40001: // Cooking
				return 169401081;
			case 40002: // Weaponsmithing
				return 169401076;
			case 40003: // Armorsmithing
				return 169401077;
			case 40004: // Tailoring
				return 169401078;
			case 40007: // Alchemy
				return 169401080;
			case 40008: // Handicrafting
				return 169401079;
			case 40010: // Menusier
				return 169401082;
		}
		return 0;
	}

}