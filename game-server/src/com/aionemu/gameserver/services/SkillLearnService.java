package com.aionemu.gameserver.services;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.ActionAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ACTION_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_REMOVE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillLearnTemplate;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, xTz, Neon
 */
public class SkillLearnService {

	public static void onLearnSkill(Player player, int skillId, int skillLevel, boolean isNew) {
		PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);
		if (skill.isProfessionSkill())
			switch (skillLevel) {
				case 1, 100, 200, 300, 400, 450, 500 -> {
					if (skillLevel != 1 || skill.isCraftingSkill()) // exclude lvl 1 tapping skills
						PacketSendUtility.broadcastPacket(player, new SM_ACTION_ANIMATION(player.getObjectId(), ActionAnimation.CRAFT_LEVEL_UP), true);
				}
			}
		if (player.getEffectController() != null) { // null on character creation
			if (player.isSpawned())
				sendPacket(player, skill, isNew);
			SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
			if (skillTemplate.isPassive())
				SkillEngine.getInstance().applyEffectDirectly(skillTemplate, skillLevel, player, player);
			if (skill.isProfessionSkill() && (skill.getSkillLevel() == 399 || skill.getSkillLevel() == 499))
				player.getController().updateNearbyQuests();
		}
		if (skill.isCraftingSkill() || skill.isMorphSkill())
			RecipeService.autoLearnRecipes(player, skillId, skillLevel);
	}

	private static void sendPacket(Player player, PlayerSkillEntry skill, boolean isNew) {
		if (skill.isProfessionSkill()) {
			if (skill.isTappingSkill())
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, isNew ? 1330004 : 1330005));
			else
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, isNew ? 1330061 : 1330064));
		} else if (isNew)
			PacketSendUtility.sendPacket(player,
				new SM_SKILL_LIST(skill, skill.isStigmaSkill() ? skill.isLinkedStigmaSkill() ? 1402891 : 1300401 : 1300050));
		else
			PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, 0));
	}

	/**
	 * Adds all missing skills and recipes that can be auto-learned for the given level range.
	 */
	public static void learnNewSkills(Player player, int fromLevel, int toLevel) {
		PlayerClass playerClass = player.getCommonData().getPlayerClass();
		PlayerClass playerStartClass = playerClass.isStartingClass() ? null : playerClass.getStartingClass();
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

	public static void learnTemporarySkill(Player player, int skillId, int skillLevel) {
		player.getSkillList().addTemporarySkill(player, skillId, skillLevel);
	}

	/**
	 * Adds auto-learned skills to the player, according to the specified level, class and race.
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
		for (SkillLearnTemplate skill : DataManager.SKILL_TREE_DATA.getSkillsForSkill(skillId, player.getPlayerClass(), player.getRace(),
			player.getLevel()))
			player.getSkillList().addSkill(player, skillId, skill.getSkillLevel());
	}

	public static boolean removeSkill(Player player, int skillId) {
		PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);
		if (skill != null) {
			player.getEffectController().removeEffect(skillId);
			player.getSkillList().removeSkill(skillId);
			PacketSendUtility.sendPacket(player, new SM_SKILL_REMOVE(skill));
			return true;
		}
		return false;
	}
}
