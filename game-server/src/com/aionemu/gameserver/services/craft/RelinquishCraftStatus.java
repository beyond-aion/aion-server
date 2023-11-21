package com.aionemu.gameserver.services.craft;

import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.craft.ExpertQuestsList;
import com.aionemu.gameserver.model.craft.MasterQuestsList;
import com.aionemu.gameserver.model.craft.Profession;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author synchro2
 */
public class RelinquishCraftStatus {

	private static final int expertMinValue = 400;
	private static final int expertMaxValue = 499;
	private static final int masterMinValue = 500;
	private static final int masterMaxValue = 549;
	private static final int expertPrice = 120895;
	private static final int masterPrice = 3497448;
	private static final int skillMessageId = 1401127;

	public static boolean relinquishExpertStatus(Player player, Profession profession) {
		return relinquishExpertStatus(player, profession, expertPrice);
	}

	public static boolean relinquishExpertStatus(Player player, Profession profession, int price) {
		return relinquishCraftStatus(player, profession, expertMinValue, expertMaxValue, price);
	}

	public static boolean relinquishMasterStatus(Player player, Profession profession) {
		return relinquishMasterStatus(player, profession, masterPrice);
	}

	public static boolean relinquishMasterStatus(Player player, Profession profession, int price) {
		return relinquishCraftStatus(player, profession, masterMinValue, masterMaxValue, price);
	}

	private static boolean relinquishCraftStatus(Player player, Profession profession, int minSkillLevel, int maxSkillLevel, int price) {
		if (profession == null || !profession.isCrafting())
			return false;
		PlayerSkillEntry skill = player.getSkillList().getSkillEntry(profession.getSkillId());
		if (skill == null || skill.getSkillLevel() < minSkillLevel || skill.getSkillLevel() > maxSkillLevel)
			return false;
		if (!decreaseKinah(player, price))
			return false;
		skill.setSkillLvl(minSkillLevel - 1);
		PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, skillMessageId));
		removeRecipesAbove(player, skill.getSkillId(), minSkillLevel);
		deleteCraftStatusQuests(skill.getSkillId(), player, maxSkillLevel < masterMinValue);
		return true;
	}

	private static boolean decreaseKinah(Player player, int basePrice) {
		if (basePrice > 0 && !player.getInventory().tryDecreaseKinah(PricesService.getPriceForService(basePrice, player.getRace()))) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
			return false;
		}
		return true;
	}

	public static void removeRecipesAbove(Player player, int skillId, int level) {
		for (RecipeTemplate recipe : DataManager.RECIPE_DATA.getRecipeTemplates()) {
			if (recipe.getSkillId() != skillId || recipe.getSkillpoint() < level) {
				continue;
			}
			player.getRecipeList().deleteRecipe(player, recipe.getId());
		}
	}

	public static void deleteCraftStatusQuests(int skillId, Player player, boolean isExpert) {
		for (int questId : MasterQuestsList.getQuestIds(skillId, player.getRace())) {
			player.getQuestStateList().deleteQuest(questId);
		}
		if (isExpert) {
			for (int questId : ExpertQuestsList.getQuestIds(skillId, player.getRace())) {
				player.getQuestStateList().deleteQuest(questId);
			}
		}
		QuestEngine.getInstance().sendCompletedQuests(player);
		player.getController().updateNearbyQuests();
	}

	public static void removeExcessCraftStatus(Player player, boolean isExpert) {
		int minValue = isExpert ? expertMinValue : masterMinValue;
		int maxValue = isExpert ? expertMaxValue : masterMaxValue;
		int skillId;
		int skillLevel;
		int maxCraftStatus = isExpert ? CraftConfig.MAX_EXPERT_CRAFTING_SKILLS : CraftConfig.MAX_MASTER_CRAFTING_SKILLS;
		int countCraftStatus;
		for (PlayerSkillEntry skill : player.getSkillList().getAllSkills()) {
			countCraftStatus = isExpert
				? CraftSkillUpdateService.getInstance().getTotalMasterCraftingSkills(player)
					+ CraftSkillUpdateService.getInstance().getTotalExpertCraftingSkills(player)
				: CraftSkillUpdateService.getInstance().getTotalMasterCraftingSkills(player);
			if (countCraftStatus > maxCraftStatus) {
				skillId = skill.getSkillId();
				skillLevel = skill.getSkillLevel();
				if (skill.isCraftingSkill() && skillLevel > minValue && skillLevel <= maxValue) {
					skill.setSkillLvl(minValue - 1);
					PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, skillMessageId));
					removeRecipesAbove(player, skillId, minValue);
					deleteCraftStatusQuests(skillId, player, isExpert);
				}
				continue;
			}
			break;
		}
		if (!isExpert) {
			removeExcessCraftStatus(player, true);
		}
	}
}
