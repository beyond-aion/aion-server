package com.aionemu.gameserver.services.craft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.StaticObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.recipe.Component;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CRAFT_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CRAFT_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.skillengine.task.CraftingTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
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
				QuestEngine.getInstance().onFailCraft(new QuestEnv(null, player, 0),
					recipetemplate.getComboProduct(1) == null ? 0 : recipetemplate.getComboProduct(1));
			}
		}

		int skillLvl = recipetemplate.getSkillpoint();
		int xpReward = (int) ((0.008 * (skillLvl + 100) * (skillLvl + 100) + 60));
		xpReward = xpReward + (xpReward * bonus / 100); // bonus
		int productItemId = critCount > 0 ? recipetemplate.getComboProduct(critCount) : recipetemplate.getProductId();

		ItemService.addItem(player, productItemId, recipetemplate.getQuantity(), true,
			new ItemUpdatePredicate(ItemAddType.CRAFTED_ITEM, ItemUpdateType.INC_ITEM_COLLECT) {

				@Override
				public boolean changeItem(Item item) {
					if (item.getItemTemplate().isWeapon() || item.getItemTemplate().isArmor()) {
						item.setItemCreator(player.getName());
						return true;
					}
					return false;
				}
			});

		if (LoggingConfig.LOG_CRAFT) {
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(productItemId);
			log.info("Player " + player.getName() + " crafted item " + productItemId + " [" + itemTemplate.getName() + "] (count: "
				+ recipetemplate.getQuantity() + ")" + (critCount > 0 ? " - critical" : ""));
		}

		int skillId = recipetemplate.getSkillId();
		int gainedCraftXp = (int) Rates.SKILL_XP_CRAFTING.calcResult(player, xpReward);
		float statRate = player.getGameStats().getStat(StatEnum.getModifier(skillId), 100).getCurrent() / 100f;
		if (statRate > 0)
			gainedCraftXp *= statRate;

		if (player.getSkillList().addSkillXp(player, skillId, gainedCraftXp, skillLvl)) {
			player.getCommonData().addExp(xpReward, Rates.XP_CRAFTING);
		} else {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_MSG_DONT_GET_PRODUCTION_EXP(DataManager.SKILL_DATA.getSkillTemplate(recipetemplate.getSkillId()).getL10n()));
		}

		if (recipetemplate.getCraftDelayId() != null) {
			player.getCraftCooldownList().addCraftCooldown(recipetemplate.getCraftDelayId(), recipetemplate.getCraftDelayTime());
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
		int skillId = recipeTemplate.getSkillId();
		VisibleObject target = player.getKnownList().getObject(targetObjId);
		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getProductId());

		if (!checkCraft(player, recipeTemplate, skillId, target, itemTemplate, craftType)) {
			sendCancelCraft(player, skillId, targetObjId, itemTemplate);
			return;
		}

		if (recipeTemplate.getDp() != null)
			player.getCommonData().addDp(-recipeTemplate.getDp());

		int intervalCap = 1200;
		switch (itemTemplate.getItemQuality()) {
			case UNIQUE:
			case EPIC:
				intervalCap = 1500;
				break;
			case MYTHIC:
				intervalCap = 1700;
				break;
		}
		int skillLvlDiff = player.getSkillList().getSkillLevel(skillId) - recipeTemplate.getSkillpoint();
		player.setCraftingTask(new CraftingTask(player, (StaticObject) target, recipeTemplate, skillLvlDiff, craftType == 1 ? 15 : 0));

		if (skillId == 40009) {
			player.getCraftingTask().setInterval(200);
		} else {
			int interval = 2500 - (skillLvlDiff * 60);
			player.getCraftingTask().setInterval(interval < intervalCap ? intervalCap : interval);
		}
		player.getCraftingTask().start();
	}

	private static boolean checkCraft(Player player, RecipeTemplate recipeTemplate, int skillId, VisibleObject target, ItemTemplate itemTemplate,
		int craftType) {

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
		if ((skillId != 40009)) {
			if (target == null || !(target instanceof StaticObject)) {
				AuditLogger.log(player, "tried to craft with incorrect target");
				return false;
			} else if (!PositionUtil.isInRange(player, target, 5, false)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_TOO_FAR_FROM_TOOL(target.getObjectTemplate().getL10n()));
				return false;
			}
		}

		if (recipeTemplate.getDp() != null && (player.getCommonData().getDp() < recipeTemplate.getDp())) {
			AuditLogger.log(player, "tried to craft without required DP count");
			return false;
		}

		if (player.isInPlayerMode(PlayerMode.RIDE)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_COMBINE_WHILE_IN_CURRENT_STANCE());
			return false;
		}

		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_INVENTORY_IS_FULL());
			return false;
		}

		if (!player.getRecipeList().isRecipePresent(recipeTemplate.getId())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_CAN_NOT_FIND_RECIPE());
			return false;
		}

		if (recipeTemplate.getCraftDelayId() != null && !player.getCraftCooldownList().isCanCraft(recipeTemplate.getCraftDelayId())) {
			AuditLogger.log(player, "tried to craft before cooldown expired");
			return false;
		}

		if (!player.getSkillList().isSkillPresent(skillId)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_CANT_USE(DataManager.SKILL_DATA.getSkillTemplate(skillId).getL10n()));
			return false;
		}

		if (player.getSkillList().getSkillLevel(skillId) < recipeTemplate.getSkillpoint()) {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_COMBINE_OUT_OF_SKILL_POINT(DataManager.SKILL_DATA.getSkillTemplate(skillId).getL10n()));
			return false;
		}

		if (craftType == 1 && !player.getInventory().decreaseByItemId(getBonusReqItem(skillId), 1)) {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_COMBINE_NO_COMPONENT_ITEM_SINGLE(DataManager.ITEM_DATA.getItemTemplate(getBonusReqItem(skillId)).getL10n()));
			return false;
		}

		for (Component component : recipeTemplate.getComponent()) {
			if (player.getInventory().getItemCountByItemId(component.getItemId()) < component.getQuantity()) {
				String itemL10n = DataManager.ITEM_DATA.getItemTemplate(component.getItemId()).getL10n();
				if (component.getQuantity() == 1)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_NO_COMPONENT_ITEM_SINGLE(itemL10n));
				else
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_NO_COMPONENT_ITEM_MULTIPLE(component.getQuantity(), itemL10n));
				return false;
			}
		}

		for (Component component : recipeTemplate.getComponent())
			player.getInventory().decreaseByItemId(component.getItemId(), component.getQuantity());

		return true;
	}

	private static void sendCancelCraft(Player player, int skillId, int targetObjId, ItemTemplate itemTemplate) {

		PacketSendUtility.sendPacket(player, new SM_CRAFT_UPDATE(skillId, itemTemplate, 0, 0, 4, 0, 0));
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
