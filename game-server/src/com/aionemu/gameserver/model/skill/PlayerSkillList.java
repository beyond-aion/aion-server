package com.aionemu.gameserver.model.skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastTable;

import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.Stigma.StigmaSkill;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author IceReaper, orfeo087, Avol, AEJTester
 * @modified Neon, bobobear
 */
public final class PlayerSkillList implements SkillList<Player> {

	private final Map<Integer, PlayerSkillEntry> basicSkills;
	private final Map<Integer, PlayerSkillEntry> stigmaSkills;
	private final Map<Integer, PlayerSkillEntry> linkedStigmaSkills;

	private final List<PlayerSkillEntry> deletedSkills;

	public PlayerSkillList() {
		this.basicSkills = new HashMap<>(0);
		this.stigmaSkills = new HashMap<>(0);
		this.linkedStigmaSkills = new HashMap<>(0);
		this.deletedSkills = new FastTable<>();
	}

	public PlayerSkillList(List<PlayerSkillEntry> skills) {
		this();
		for (PlayerSkillEntry entry : skills) {
			switch (entry.getSkillType()) {
				case 0:
					basicSkills.put(entry.getSkillId(), entry);
					break;
				case 1:
					stigmaSkills.put(entry.getSkillId(), entry);
					break;
				case 3:
					linkedStigmaSkills.put(entry.getSkillId(), entry);
					break;
			}
		}
	}

	/**
	 * Returns array with all skills
	 */
	public List<PlayerSkillEntry> getAllSkills() {
		List<PlayerSkillEntry> skills = new FastTable<>();
		skills.addAll(basicSkills.values());
		skills.addAll(stigmaSkills.values());
		skills.addAll(linkedStigmaSkills.values());
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

	public List<PlayerSkillEntry> getLinkedStigmaSkills() {
		List<PlayerSkillEntry> skills = new FastTable<>();
		skills.addAll(linkedStigmaSkills.values());
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
		if (linkedStigmaSkills.containsKey(skillId))
			return linkedStigmaSkills.get(skillId);
		return stigmaSkills.get(skillId);
	}

	@Override
	public boolean addSkill(Player player, int skillId, int skillLevel) {
		return addSkill(player, skillId, skillLevel, false, false, PersistentState.NEW);
	}

	public boolean addStigmaSkill(Player player, int skillId, int skillLevel) {
		return addSkill(player, skillId, skillLevel, true, false, PersistentState.NEW);
	}

	public boolean addLinkedStigmaSkill(Player player, int skillId, int skillLevel) {
		return addSkill(player, skillId, skillLevel, true, true, PersistentState.NEW);
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
		return addSkill(player, skillId, skillLevel, false, false, PersistentState.NOACTION);
	}

	public void addLinkedStigmaSkill(Player player, List<StigmaSkill> skills, boolean equipedByNpc) {
		boolean isNew = false;
		for (StigmaSkill sSkill : skills) {
			PlayerSkillEntry skill = new PlayerSkillEntry(sSkill.getSkillId(), sSkill.getSkillLvl(), 3, PersistentState.NEW);
			this.linkedStigmaSkills.put(sSkill.getSkillId(), skill);
			if (equipedByNpc) {
				if (!isNew) {
					isNew = true;
					Integer lvlMsg = skills.size();
					PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, 1402891, Integer.toString(lvlMsg), false, true));
				} else
					PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, 0, null, false, true));
			}
		}
	}

	public void addStigmaSkill(Player player, List<StigmaSkill> skills, boolean equipedByNpc) {
		boolean isNew = false;
		for (StigmaSkill sSkill : skills) {
			PlayerSkillEntry skill = new PlayerSkillEntry(sSkill.getSkillId(), sSkill.getSkillLvl(), 1, PersistentState.NEW);
			this.stigmaSkills.put(sSkill.getSkillId(), skill);
			if (equipedByNpc) {
				if (!isNew) {
					isNew = true;
					Integer lvlMsg = skills.size();
					PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, 1300401, Integer.toString(lvlMsg), false, true));
				} else
					PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(skill, 0, null, false, true));
			}
		}
	}

	private synchronized boolean addSkill(Player player, int skillId, int skillLevel, boolean isStigma, boolean isLinkedStigma, PersistentState state) {
		PlayerSkillEntry existingSkill;
		if (isStigma)
			existingSkill = isLinkedStigma ? linkedStigmaSkills.get(skillId) : stigmaSkills.get(skillId);
		else
			existingSkill = basicSkills.get(skillId);

		boolean isNew = false;
		if (existingSkill != null) {
			if (existingSkill.getSkillLevel() >= skillLevel) {
				return false;
			}
			existingSkill.setSkillLvl(skillLevel);
		} else {
			if (isStigma)
				if (isLinkedStigma)
					linkedStigmaSkills.put(skillId, new PlayerSkillEntry(skillId, skillLevel, 3, state));
				else
					stigmaSkills.put(skillId, new PlayerSkillEntry(skillId, skillLevel, 1, state));
			else {
				basicSkills.put(skillId, new PlayerSkillEntry(skillId, skillLevel, 0, state));
				isNew = true;
			}
		}
		if (player.isSpawned())
			// TODO refine
			if (isNew) {
				int level = 1;
				List<PlayerSkillEntry> newSkillsByName = new FastTable<>();
				SkillTemplate st = DataManager.SKILL_DATA.getSkillTemplate(skillId);
				List<PlayerSkillEntry> pse = player.getSkillList().getAllSkills();
				for (PlayerSkillEntry skill : pse) {
					if (skill.getSkillName().equalsIgnoreCase(st.getName()))
						newSkillsByName.add(skill);
				}
				if (newSkillsByName.size() > 1)
					level = newSkillsByName.size();
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1300050, Integer.toString(level), false,
					true));
			} else
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
		return basicSkills.containsKey(skillId) || stigmaSkills.containsKey(skillId) || linkedStigmaSkills.containsKey(skillId);
	}

	@Override
	public int getSkillLevel(int skillId) {
		if (basicSkills.containsKey(skillId))
			return basicSkills.get(skillId).getSkillLevel();
		if (linkedStigmaSkills.containsKey(skillId))
			return linkedStigmaSkills.get(skillId).getSkillLevel();
		return stigmaSkills.get(skillId).getSkillLevel();
	}

	@Override
	public synchronized boolean removeSkill(int skillId) {
		PlayerSkillEntry entry = basicSkills.get(skillId);
		if (entry == null) {
			entry = stigmaSkills.get(skillId);
			entry = linkedStigmaSkills.get(skillId);
			return false;
		} else {
			entry.setPersistentState(PersistentState.DELETED);
			deletedSkills.add(entry);
			basicSkills.remove(skillId);
			stigmaSkills.remove(skillId);
			linkedStigmaSkills.remove(skillId);
			return true;
		}
	}

	@Override
	public int size() {
		return basicSkills.size() + stigmaSkills.size() + linkedStigmaSkills.size();
	}

	/**
	 * @param player
	 * @param skillId
	 */
	private void sendMessage(Player player, int skillId, boolean isNew) {
		switch (skillId) {
			case 30001:
			case 30002:
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330005, null, false, false));
				break;
			case 30003:
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330005, null, false, false));
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
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330061, null, false, false));
				break;
			default:
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 0, null, (player.getSkillList()
					.getSkillEntry(skillId).isStigma() ? false : true), isNew));
		}
	}
}
