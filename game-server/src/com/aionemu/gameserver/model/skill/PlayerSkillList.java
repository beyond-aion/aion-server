package com.aionemu.gameserver.model.skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastTable;

import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.Stigma.StigmaSkill;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author IceReaper, orfeo087, Avol, AEJTester
 * @modified Neon
 */
public final class PlayerSkillList implements SkillList<Player> {

	private final Map<Integer, PlayerSkillEntry> basicSkills;
	private final Map<Integer, PlayerSkillEntry> stigmaSkills;

	private final List<PlayerSkillEntry> deletedSkills;

	public PlayerSkillList() {
		this.basicSkills = new HashMap<Integer, PlayerSkillEntry>(0);
		this.stigmaSkills = new HashMap<Integer, PlayerSkillEntry>(0);
		this.deletedSkills = new FastTable<PlayerSkillEntry>();
	}

	public PlayerSkillList(List<PlayerSkillEntry> skills) {
		this();
		for (PlayerSkillEntry entry : skills) {
			if (entry.isStigma())
				stigmaSkills.put(entry.getSkillId(), entry);
			else
				basicSkills.put(entry.getSkillId(), entry);
		}
	}

	/**
	 * Returns array with all skills
	 */
	public List<PlayerSkillEntry> getAllSkills() {
		List<PlayerSkillEntry> skills = new FastTable<>();
		skills.addAll(basicSkills.values());
		skills.addAll(stigmaSkills.values());
		return skills;
	}

	public List<PlayerSkillEntry> getBasicSkills() {
		List<PlayerSkillEntry> skills = new FastTable<>();
		skills.addAll(basicSkills.values());
		return skills;
	}

	public List<PlayerSkillEntry> getStigmaSkills() {
		List<PlayerSkillEntry> skills = new FastTable<>();
		skills.addAll(stigmaSkills.values());
		return skills;
	}

	public List<PlayerSkillEntry> getDeletedSkills() {
		List<PlayerSkillEntry> skills = new FastTable<>();
		skills.addAll(deletedSkills);
		return skills;
	}

	public PlayerSkillEntry getSkillEntry(int skillId) {
		if (basicSkills.containsKey(skillId))
			return basicSkills.get(skillId);
		return stigmaSkills.get(skillId);
	}

	@Override
	public boolean addSkill(Player player, int skillId, int skillLevel) {
		return addSkill(player, skillId, skillLevel, false, PersistentState.NEW);
	}

	public boolean addStigmaSkill(Player player, int skillId, int skillLevel) {
		return addSkill(player, skillId, skillLevel, true, PersistentState.NEW);
	}

	/**
	 * Add temporary skill which will not be saved in db
	 * 
	 * @param player
	 * @param skillId
	 * @param skillLevel
	 * @param msg
	 * @return
	 */
	public boolean addAbyssSkill(Player player, int skillId, int skillLevel) {
		return addSkill(player, skillId, skillLevel, false, PersistentState.NOACTION);
	}

	public void addStigmaSkill(Player player, List<StigmaSkill> skills, boolean equipedByNpc) {
		for (StigmaSkill sSkill : skills) {
			PlayerSkillEntry skill = new PlayerSkillEntry(sSkill.getSkillId(), true, sSkill.getSkillLvl(), PersistentState.NEW);
			this.stigmaSkills.put(sSkill.getSkillId(), skill);
			if (equipedByNpc) {
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, 1300401, false));
			}
		}
	}

	private synchronized boolean addSkill(Player player, int skillId, int skillLevel, boolean isStigma, PersistentState state) {
		PlayerSkillEntry existingSkill = isStigma ? stigmaSkills.get(skillId) : basicSkills.get(skillId);

		boolean isNew = false;
		if (existingSkill != null) {
			if (existingSkill.getSkillLevel() >= skillLevel) {
				return false;
			}
			existingSkill.setSkillLvl(skillLevel);
		} else {
			if (isStigma)
				stigmaSkills.put(skillId, new PlayerSkillEntry(skillId, true, skillLevel, state));
			else {
				basicSkills.put(skillId, new PlayerSkillEntry(skillId, false, skillLevel, state));
				isNew = true;
			}
		}
		if (player.isSpawned())
			sendMessage(player, skillId, isNew);
		return true;
	}

	/**
	 * @param player
	 * @param skillId
	 * @param xpReward
	 * @return
	 */
	public boolean addSkillXp(Player player, int skillId, int xpReward, int objSkillPoints) {
		PlayerSkillEntry skillEntry = getSkillEntry(skillId);
		int skillLvl = skillEntry.getSkillLevel();
		int maxDiff = 40;
		int SkillLvlDiff = skillLvl - objSkillPoints;
		if (maxDiff < SkillLvlDiff)
			return false;

		switch (skillEntry.getSkillId()) {
			case 30001:
				if (skillLvl == 49)
					return false; // disable exp gain to force mastering upgrade via npc
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
				player.getRecipeList().autoLearnRecipe(player, skillId, skillLvl);
		}
		boolean updateSkill = skillEntry.addSkillXp(player, xpReward);
		if (updateSkill)
			sendMessage(player, skillId, false);
		return true;
	}

	@Override
	public boolean isSkillPresent(int skillId) {
		return basicSkills.containsKey(skillId) || stigmaSkills.containsKey(skillId);
	}

	@Override
	public int getSkillLevel(int skillId) {
		if (basicSkills.containsKey(skillId))
			return basicSkills.get(skillId).getSkillLevel();
		return stigmaSkills.get(skillId).getSkillLevel();
	}

	@Override
	public synchronized boolean removeSkill(int skillId) {
		PlayerSkillEntry entry = basicSkills.get(skillId);
		if (entry == null)
			entry = stigmaSkills.get(skillId);
		if (entry != null) {
			entry.setPersistentState(PersistentState.DELETED);
			deletedSkills.add(entry);
			basicSkills.remove(skillId);
			stigmaSkills.remove(skillId);
		}
		return entry != null;
	}

	@Override
	public int size() {
		return basicSkills.size() + stigmaSkills.size();
	}

	/**
	 * @param player
	 * @param skillId
	 */
	private void sendMessage(Player player, int skillId, boolean isNew) {
		switch (skillId) {
			case 30001:
			case 30002:
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330005, false));
				break;
			case 30003:
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330005, false));
				break;
			case 40001:
			case 40002:
			case 40003:
			case 40004:
			case 40005:
			case 40006:
			case 40007:
			case 40008:
			case 40009:
			case 40010:
				if (player.getSkillList().getSkillLevel(skillId) == 399 || player.getSkillList().getSkillLevel(skillId) == 499) {
					player.getController().updateNearbyQuests();
				}
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330061, false));
				break;
			default:
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 0, isNew));
		}
	}
}
