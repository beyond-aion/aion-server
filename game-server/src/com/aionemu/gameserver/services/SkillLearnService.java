package com.aionemu.gameserver.services;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_REMOVE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillLearnTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, xTz
 * @reworked Neon
 */
public class SkillLearnService {

	public static void onLearnSkill(Player player, int skillId, int skillLevel, boolean isNew) {
		PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);
		if (player.isSpawned())
			sendPacket(player, skill, isNew);
		if (DataManager.SKILL_DATA.getSkillTemplate(skillId).isPassive() && player.getEffectController() != null)
			SkillEngine.getInstance().applyEffectDirectly(skillId, skillLevel, player, player, 0);
		if (skill.isProfessionSkill() && (skill.getSkillLevel() == 399 || skill.getSkillLevel() == 499))
			player.getController().updateNearbyQuests();
		if (skill.isCraftingSkill() || skill.isMorphSkill())
			RecipeService.autoLearnRecipes(player, skillId, skillLevel);
	}

	private static void sendPacket(Player player, PlayerSkillEntry skill, boolean isNew) {
		if (skill.isProfessionSkill())
			PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, isNew ? 1330061 : 1330005, false));
		else if (isNew)
			PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, skill.isStigmaSkill() ? skill.isLinkedStigmaSkill() ? 1402891 : 1300401 : 1300050, true));
		else
			PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, 0, false));
	}

	/**
	 * Adds all missing skills and recipes that can be auto-learned for the given level range.
	 * 
	 * @param player
	 * @param fromLevel
	 * @param toLevel
	 */
	public static void learnNewSkills(Player player, int fromLevel, int toLevel) {
		PlayerClass playerClass = player.getCommonData().getPlayerClass();
		PlayerClass playerStartClass = playerClass.isStartingClass() ? null : PlayerClass.getStartingClassFor(playerClass);
		for (int level = toLevel; level >= fromLevel; level--) { // reversed order to add only the highest of each skill (more efficient)
			if (level < 10 && playerStartClass != null) // add missing start class skills if already switched class
				autoLearnSkills(player, level, playerStartClass, player.getRace());
			autoLearnSkills(player, level, playerClass, player.getRace());
		}

		// upgrade human gathering to daeva essence tapping
		if (toLevel >= 10 && player.getCommonData().isDaeva() && player.getSkillList().isSkillPresent(30001)) {
			if (!player.getSkillList().isSkillPresent(30002))
				player.getSkillList().addSkill(player, 30002, player.getSkillList().getSkillLevel(30001));
			removeSkill(player, 30001);
		}
	}

	/**
	 * Adds auto-learned skills to the player, according to the specified level, class and race.
	 * 
	 * @param player
	 * @param level
	 * @param playerClass
	 * @param playerRace
	 */
	private static void autoLearnSkills(Player player, int level, PlayerClass playerClass, Race playerRace) {
		for (SkillLearnTemplate template : DataManager.SKILL_TREE_DATA.getTemplatesFor(playerClass, level, playerRace)) {
			if (!template.isAutolearn())
				continue;
			if (template.getSkillId() == 30001 && !playerClass.isStartingClass()) // no human gathering for main classes
				continue;

			player.getSkillList().addSkill(player, template.getSkillId(), template.getSkillLevel());
		}
	}

	public static void learnSkillBook(Player player, int skillId) {
		for (int i = player.getLevel(); i > 0; i--) {
			for (SkillLearnTemplate skill : DataManager.SKILL_TREE_DATA.getTemplatesFor(player.getPlayerClass(), i, player.getRace()))
				if (skillId == skill.getSkillId()) {
					player.getSkillList().addSkill(player, skillId, skill.getSkillLevel());
					break;
				}
		}
	}

	public static boolean removeSkill(Player player, int skillId) {
		PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);
		if (skill != null) {
			PacketSendUtility.sendPacket(player, new SM_SKILL_REMOVE(skill));
			player.getSkillList().removeSkill(skillId);
			player.getEffectController().removeEffect(skillId);
			return true;
		}
		return false;
	}

	public static int getSkillMinLevel(int skillId, int playerLevel, int wantedSkillLevel) {
		int nearestMinLevel = 0;
		for (SkillLearnTemplate template : DataManager.SKILL_TREE_DATA.getTemplatesForSkill(skillId)) {
			if (template.getMinLevel() <= playerLevel && template.getSkillLevel() <= wantedSkillLevel)
				nearestMinLevel = Math.max(template.getMinLevel(), nearestMinLevel);
		}
		return nearestMinLevel > 0 ? nearestMinLevel : playerLevel;
	}
}
