package com.aionemu.gameserver.model.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.skillengine.model.SkillLearnTemplate;

/**
 * @author IceReaper, orfeo087, Avol, AEJTester
 * @modified Neon
 */
public final class PlayerSkillList implements SkillList<Player> {

	private final Map<Integer, PlayerSkillEntry> skills;
	private final List<PlayerSkillEntry> deletedSkills;

	public PlayerSkillList() {
		this(new ArrayList<>());
	}

	public PlayerSkillList(List<PlayerSkillEntry> playerSkills) {
		this.skills = new HashMap<>();
		this.deletedSkills = new ArrayList<>();
		for (PlayerSkillEntry entry : playerSkills)
			skills.put(entry.getSkillId(), entry);
	}

	public List<PlayerSkillEntry> getAllSkills() {
		return new ArrayList<>(skills.values());
	}

	public List<PlayerSkillEntry> getDeletedSkills() {
		return new ArrayList<>(deletedSkills);
	}

	public PlayerSkillEntry getSkillEntry(int skillId) {
		return skills.get(skillId);
	}

	@Override
	public boolean addSkill(Player player, int skillId, int skillLevel) {
		return addSkill(player, skillId, skillLevel, false);
	}

	/**
	 * Adds a temporary skill which will not be saved in database.
	 * 
	 * @param player
	 * @param skillId
	 * @param skillLevel
	 * @return
	 */
	public boolean addTemporarySkill(Player player, int skillId, int skillLevel) {
		return addSkill(player, skillId, skillLevel, true);
	}

	private synchronized boolean addSkill(Player player, int skillId, int skillLevel, boolean isTemporary) {
		PlayerSkillEntry existingSkill = skills.get(skillId);
		boolean isNew = true;
		if (existingSkill != null) {
			if (skillLevel <= existingSkill.getSkillLevel())
				return false;
			existingSkill.setSkillLvl(skillLevel);
			isNew = false;
		} else {
			skills.put(skillId, new PlayerSkillEntry(player, skillId, skillLevel, isTemporary ? PersistentState.NOACTION : PersistentState.NEW));
			List<SkillLearnTemplate> learnTemplates = DataManager.SKILL_TREE_DATA.getSkillsForSkill(skillId, player.getPlayerClass(), player.getRace(), player.getLevel());
			for (SkillLearnTemplate learnTemplate : learnTemplates) {
				if (learnTemplate.getLearnSkill() != null && skills.get(learnTemplate.getLearnSkill()) != null) {
					isNew = false;
					break;
				}
			}
		}
		SkillLearnService.onLearnSkill(player, skillId, skillLevel, isNew);
		return true;
	}

	/**
	 * Only for usage with gathering and crafting skills.
	 */
	@SuppressWarnings("fallthrough")
	public boolean addSkillXp(Player player, int skillId, int xpReward, int objSkillLvl) {
		PlayerSkillEntry skill = getSkillEntry(skillId);
		int skillLvl = skill.getSkillLevel();
		if (skillLvl - objSkillLvl > 40)
			return false;

		switch (skillId) {
			case 30001:
				if (skillLvl == 49)
					return false; // human gathering is capped at 49 points
			case 30002:
			case 30003:
				if (skillLvl == 449 || skillLvl >= 499 && CraftConfig.DISABLE_AETHER_AND_ESSENCE_TAPPING_CAP)
					break; // break here to enable gather exp on master max lvl
			case 40001:
			case 40002:
			case 40003:
			case 40004:
			case 40007:
			case 40008:
			case 40010:
				switch (skillLvl) {
					case 99:
					case 199:
					case 299:
					case 399:
					case 449:
					case 499:
					case 549:
						return false; // disable exp gain to force mastering upgrade via npc
				}
		}

		int requiredExp = (int) (0.23 * (skillLvl + 17.2) * (skillLvl + 17.2));
		if (skill.getCurrentXp() + xpReward >= requiredExp) {
			skill.setCurrentXp(0);
			skill.setSkillLvl(skillLvl + 1);
			SkillLearnService.onLearnSkill(player, skillId, skillLvl, false);
		} else
			skill.setCurrentXp(skill.getCurrentXp() + xpReward);
		return true;
	}

	@Override
	public boolean isSkillPresent(int skillId) {
		return skills.containsKey(skillId);
	}

	@Override
	public int getSkillLevel(int skillId) {
		return skills.get(skillId).getSkillLevel();
	}

	@Override
	public synchronized boolean removeSkill(int skillId) {
		PlayerSkillEntry entry = skills.remove(skillId);
		if (entry != null) {
			entry.setPersistentState(PersistentState.DELETED);
			deletedSkills.add(entry);
		}
		return entry != null;
	}

	@Override
	public int size() {
		return skills.size();
	}
}
