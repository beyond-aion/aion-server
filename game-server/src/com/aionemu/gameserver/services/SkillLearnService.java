package com.aionemu.gameserver.services;

import java.util.List;

import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.model.templates.item.StigmaSkill;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_REMOVE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillLearnTemplate;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, xTz
 */
public class SkillLearnService {

	/**
	 * @param player
	 */
	public static void addNewSkills(Player player) {
		int level = player.getLevel();
		PlayerClass playerClass = player.getPlayerClass();
		Race playerRace = player.getRace();

		if (player.getCommonData().isDaeva() && player.getSkillList().getSkillEntry(30001) != null) {
			int skillLevel = player.getSkillList().getSkillLevel(30001);
			removeSkill(player, 30001);
			PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getBasicSkills()));
			// Why adding after the packet ?
			player.getSkillList().addSkill(player, 30002, skillLevel);
		}
		if (player.getCommonData().isDaeva() && !(player.getSkillList().getSkillEntry(40009) != null))
			player.getSkillList().addSkill(player, 40009, 1);
		addSkills(player, level, playerClass, playerRace);
	}

	public static void onEnterWorld(Player player) {
		int level = player.getLevel();
		PlayerClass playerClass = player.getPlayerClass();
		Race playerRace = player.getRace();

		if (playerClass.isStartingClass()) {
			for (int i = 1; i <= level; i++)
				addSkills(player, i, playerClass, playerRace);
			return;
		}
		for (int i = 1; i <= level; i++)
			addSkills(player, i, playerClass, playerRace);

		PlayerClass startinClass = PlayerClass.getStartingClassFor(playerClass);
		for (int i = 1; i < 10; i++)
			addSkills(player, i, startinClass, playerRace);

		if (player.getSkillList().getSkillEntry(30001) != null) {
			int skillLevel = player.getSkillList().getSkillLevel(30001);
			player.getSkillList().removeSkill(30001);
			// Not sure about that, mysterious code
			PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getBasicSkills()));
			for (PlayerSkillEntry stigmaSkill : player.getSkillList().getStigmaSkills())
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(stigmaSkill));
			for (PlayerSkillEntry linkedStigmaSkill : player.getSkillList().getLinkedStigmaSkills())
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(linkedStigmaSkill));
			// Why adding after the packet ?
			player.getSkillList().addSkill(player, 30002, skillLevel);
		}
	}

	/**
	 * Recursively check missing skills and add them to player
	 * 
	 * @param player
	 */
	public static void addMissingSkills(Player player) {
		int level = player.getLevel();
		PlayerClass playerClass = player.getPlayerClass();
		Race playerRace = player.getRace();
		for (int i = 0; i <= level; i++)
			addSkills(player, i, playerClass, playerRace);

		if (!playerClass.isStartingClass()) {
			PlayerClass startinClass = PlayerClass.getStartingClassFor(playerClass);
			for (int i = 1; i < 10; i++)
				addSkills(player, i, startinClass, playerRace);

			if (player.getSkillList().getSkillEntry(30001) != null) {
				int skillLevel = player.getSkillList().getSkillLevel(30001);
				player.getSkillList().removeSkill(30001);
				// Not sure about that, mysterious code
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getBasicSkills()));
				for (PlayerSkillEntry stigmaSkill : player.getSkillList().getStigmaSkills())
					PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(stigmaSkill));
				for (PlayerSkillEntry linkedStigmaSkill : player.getSkillList().getLinkedStigmaSkills())
					PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(linkedStigmaSkill));
				// Why adding after the packet ?
				player.getSkillList().addSkill(player, 30002, skillLevel);
			}
		}
	}

	/**
	 * Adds skill to player according to the specified level, class and race
	 * 
	 * @param player
	 * @param level
	 * @param playerClass
	 * @param playerRace
	 */
	private static void addSkills(Player player, int level, PlayerClass playerClass, Race playerRace) {
		SkillLearnTemplate[] skillTemplates = DataManager.SKILL_TREE_DATA.getTemplatesFor(playerClass, level, playerRace);
		PlayerSkillList playerSkillList = player.getSkillList();

		for (SkillLearnTemplate template : skillTemplates) {
			if (!checkLearnIsPossible(player, playerSkillList, template))
				continue;

			if (template.isStigma())
				if (template.isLinkedStigma())
					playerSkillList.addLinkedStigmaSkill(player, template.getSkillId(), template.getSkillLevel());
				else
					playerSkillList.addStigmaSkill(player, template.getSkillId(), template.getSkillLevel());
			else
				playerSkillList.addSkill(player, template.getSkillId(), template.getSkillLevel());
		}
		List<StigmaSkill> mSkills = StigmaService.getMissingStigmaSkills(player);
		for (StigmaSkill template : mSkills)
			playerSkillList.addStigmaSkill(player, template.getSkillId(), template.getSkillLvl());
	}

	/**
	 * Check SKILL_AUTOLEARN property Check skill already learned Check skill template auto-learn attribute
	 * 
	 * @param playerSkillList
	 * @param template
	 * @return
	 */
	private static boolean checkLearnIsPossible(Player player, PlayerSkillList playerSkillList, SkillLearnTemplate template) {
		if (playerSkillList.isSkillPresent(template.getSkillId()))
			return false;

		if (player.havePermission(MembershipConfig.STIGMA_AUTOLEARN) && template.isStigma())
			return true;

		if (template.isAutolearn())
			return true;

		return false;
	}

	public static void learnSkillBook(Player player, int skillId) {
		SkillLearnTemplate[] skillTemplates = null;
		int maxLevel = 0;
		SkillTemplate passiveSkill = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		for (int i = 1; i <= player.getLevel(); i++) {
			skillTemplates = DataManager.SKILL_TREE_DATA.getTemplatesFor(player.getPlayerClass(), i, player.getRace());

			for (SkillLearnTemplate skill : skillTemplates)
				if (skillId == skill.getSkillId()) {
					if (skill.getSkillLevel() > maxLevel)
						maxLevel = skill.getSkillLevel();
				}
		}
		player.getSkillList().addSkill(player, skillId, maxLevel);
		if (passiveSkill.isPassive())
			player.getController().updatePassiveStats();
	}

	public static void removeSkill(Player player, int skillId) {
		if (player.getSkillList().isSkillPresent(skillId)) {
			Integer skillLevel = player.getSkillList().getSkillLevel(skillId);
			PacketSendUtility.sendPacket(player, new SM_SKILL_REMOVE(skillId, skillLevel, player.getSkillList().getSkillEntry(skillId).getSkillType()));
			player.getSkillList().removeSkill(skillId);
			SkillTemplate skill = DataManager.SKILL_DATA.getSkillTemplate(skillId);
			if (skill != null && skill.isPassive())
				player.getEffectController().removeEffect(skillId);
		}
	}

	public static void addSkill(Player player, int skillId) {
		player.getSkillList().addSkill(player, skillId, 1);
		SkillTemplate skill = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (skill != null && skill.isPassive()) {
			SkillEngine.getInstance().applyEffectDirectly(skillId, player, player, 0);
		}
	}

	public static int getSummonSkillLearnLevel(int skillId, int playerLevel, int wantedSkillLevel) {
		SkillLearnTemplate[] skillTemplates = DataManager.SKILL_TREE_DATA.getTemplatesForSkill(skillId);
		int learnFinishes = 0;
		int maxLevel = 0;

		for (SkillLearnTemplate template : skillTemplates) {
			if (maxLevel < template.getSkillLevel())
				maxLevel = template.getSkillLevel();
		}

		// no data in skill tree, use as wanted
		if (maxLevel == 0)
			return wantedSkillLevel;

		learnFinishes = playerLevel + maxLevel;

		if (learnFinishes > DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel())
			learnFinishes = DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel();

		return Math.max(wantedSkillLevel, Math.min(playerLevel - (learnFinishes - maxLevel) + 1, maxLevel));
	}

	public static int getSummonSkillMinLevel(int skillId, int playerLevel, int wantedSkillLevel) {
		SkillLearnTemplate[] skillTemplates = DataManager.SKILL_TREE_DATA.getTemplatesForSkill(skillId);
		SkillLearnTemplate foundTemplate = null;

		for (SkillLearnTemplate template : skillTemplates) {
			if (template.getSkillLevel() <= wantedSkillLevel && template.getMinLevel() <= playerLevel)
				foundTemplate = template;
		}

		if (foundTemplate == null)
			return playerLevel;

		return foundTemplate.getMinLevel();
	}

}
