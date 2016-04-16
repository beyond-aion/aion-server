package com.aionemu.gameserver.services.craft;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.model.templates.CraftLearnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.utils.PacketSendUtility;

import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * @author MrPoke, sphinx
 * @modified Imaginary, Pad
 */

public class CraftSkillUpdateService {

	private static final Logger log = LoggerFactory.getLogger(CraftSkillUpdateService.class);

	private static final Map<Integer, CraftLearnTemplate> npcBySkill = new FastMap<>();
	private static final Map<Integer, Integer> cost = new FastMap<>();
	private static final List<Integer> craftingSkillIds = new FastTable<>();

	public static final CraftSkillUpdateService getInstance() {
		return SingletonHolder.instance;
	}

	private CraftSkillUpdateService() {
		// Asmodian
		npcBySkill.put(204096, new CraftLearnTemplate(30002, false, "Extract Vitality"));
		npcBySkill.put(830150, new CraftLearnTemplate(30002, false, "Extract Vitality"));
		npcBySkill.put(204257, new CraftLearnTemplate(30003, false, "Extract Aether"));
		npcBySkill.put(830148, new CraftLearnTemplate(30003, false, "Extract Aether"));

		npcBySkill.put(204100, new CraftLearnTemplate(40001, true, "Cooking"));
		npcBySkill.put(830142, new CraftLearnTemplate(40001, true, "Cooking"));
		npcBySkill.put(204104, new CraftLearnTemplate(40002, true, "Weaponsmithing"));
		npcBySkill.put(830146, new CraftLearnTemplate(40002, true, "Weaponsmithing"));
		npcBySkill.put(204106, new CraftLearnTemplate(40003, true, "Armorsmithing"));
		npcBySkill.put(830144, new CraftLearnTemplate(40003, true, "Armorsmithing"));
		npcBySkill.put(204110, new CraftLearnTemplate(40004, true, "Tailoring"));
		npcBySkill.put(830136, new CraftLearnTemplate(40004, true, "Tailoring"));
		npcBySkill.put(204102, new CraftLearnTemplate(40007, true, "Alchemy"));
		npcBySkill.put(830138, new CraftLearnTemplate(40007, true, "Alchemy"));
		npcBySkill.put(204108, new CraftLearnTemplate(40008, true, "Handicrafting"));
		npcBySkill.put(830140, new CraftLearnTemplate(40008, true, "Handicrafting"));
		npcBySkill.put(798452, new CraftLearnTemplate(40010, true, "Menusier"));
		npcBySkill.put(798456, new CraftLearnTemplate(40010, true, "Menusier"));

		// Elyos
		npcBySkill.put(203780, new CraftLearnTemplate(30002, false, "Extract Vitality"));
		npcBySkill.put(830066, new CraftLearnTemplate(30002, false, "Extract Vitality"));
		npcBySkill.put(203782, new CraftLearnTemplate(30003, false, "Extract Aether"));
		npcBySkill.put(830064, new CraftLearnTemplate(30003, false, "Extract Aether"));

		npcBySkill.put(203784, new CraftLearnTemplate(40001, true, "Cooking"));
		npcBySkill.put(830058, new CraftLearnTemplate(40001, true, "Cooking"));
		npcBySkill.put(203788, new CraftLearnTemplate(40002, true, "Weaponsmithing"));
		npcBySkill.put(830062, new CraftLearnTemplate(40002, true, "Weaponsmithing"));
		npcBySkill.put(203790, new CraftLearnTemplate(40003, true, "Armorsmithing"));
		npcBySkill.put(830060, new CraftLearnTemplate(40003, true, "Armorsmithing"));
		npcBySkill.put(203793, new CraftLearnTemplate(40004, true, "Tailoring"));
		npcBySkill.put(830052, new CraftLearnTemplate(40004, true, "Tailoring"));
		npcBySkill.put(203786, new CraftLearnTemplate(40007, true, "Alchemy"));
		npcBySkill.put(830054, new CraftLearnTemplate(40007, true, "Alchemy"));
		npcBySkill.put(203792, new CraftLearnTemplate(40008, true, "Handicrafting"));
		npcBySkill.put(830056, new CraftLearnTemplate(40008, true, "Handicrafting"));
		npcBySkill.put(798450, new CraftLearnTemplate(40010, true, "Menusier"));
		npcBySkill.put(798454, new CraftLearnTemplate(40010, true, "Menusier"));

		
		cost.put(0, 3500);
		cost.put(99, 17000);
		cost.put(199, 115000);
		cost.put(299, 460000);
		cost.put(399, 0);
		cost.put(449, 6004900);
		cost.put(499, 12000000);

		
		craftingSkillIds.add(40001);
		craftingSkillIds.add(40002);
		craftingSkillIds.add(40003);
		craftingSkillIds.add(40004);
		craftingSkillIds.add(40007);
		craftingSkillIds.add(40008);
		craftingSkillIds.add(40010);

		log.info("CraftSkillUpdateService: Initialized.");
	}

	/**
	 * returns the respective CraftingLearnTemplate for a specific Npc
	 * 
	 * @param npc
	 * @return the corresponding CraftingLearnTemplate
	 */
	public CraftLearnTemplate getCLTemplateByNpc(Npc npc) {
		return npcBySkill.get(npc.getNpcId());
	}

	/**
	 * handles the crafting skill learning
	 * 
	 * @param player
	 * @param npc
	 */
	public void learnSkill(Player player, Npc npc) {
		if (player.getLevel() < 10)
			return;
		final CraftLearnTemplate template = npcBySkill.get(npc.getNpcId());
		if (template == null)
			return;
		final int skillId = template.getSkillId();
		if (skillId == 0)
			return;

		int skillLvl = 0;
		PlayerSkillList skillList = player.getSkillList();
		if (skillList.isSkillPresent(skillId))
			skillLvl = skillList.getSkillLevel(skillId);

		if (!cost.containsKey(skillLvl)) {
			if (skillLvl < cost.keySet().stream().max(Comparator.naturalOrder()).get())
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DONT_RANK_UP());
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_COMBINE_CBT_CAP());
			return;
		}

		// Retail : Max 2 expert crafting skill
		if (isCraftingSkill(skillId) && skillLvl == 399 && !canLearnMoreExpertCraftingSkill(player)) {
			return;
		}

		// Retail : Max 1 master crafting skill
		if (isCraftingSkill(skillId) && skillLvl == 499 && !canLearnMoreMasterCraftingSkill(player)) {
			return;
		}

		// Prevents player from buying expert craft upgrade (399 to 400)
		if (skillLvl == 399) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CRAFT_CANT_EXTEND_MONEY());
			return;
		}

		// There is no upgrade payment for Essence and Aether tapping at 449, skip.
		if (skillLvl == 449 && (skillId == 30002 || skillId == 30003)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DONT_RANK_UP());
			return;
		}

		// You must do quest before being able to buy master update (499 to 500)
		if (skillLvl == 499 && ((skillId == 40001 && (!player.isCompleteQuest(29039) || !player.isCompleteQuest(19039)))
			|| (skillId == 40002 && (!player.isCompleteQuest(29009) || !player.isCompleteQuest(19009)))
			|| (skillId == 40003 && (!player.isCompleteQuest(29015) || !player.isCompleteQuest(19015)))
			|| (skillId == 40004 && (!player.isCompleteQuest(29021) || !player.isCompleteQuest(19021)))
			|| (skillId == 40007 && (!player.isCompleteQuest(29033) || !player.isCompleteQuest(19033)))
			|| (skillId == 40008 && (!player.isCompleteQuest(29027) || !player.isCompleteQuest(19027)))
			|| (skillId == 40010 && (!player.isCompleteQuest(29058) || !player.isCompleteQuest(19058))))) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CRAFT_CANT_EXTEND_GRAND_MASTER());
			return;
		}

		// There is no Master upgrade for Aether and Essence tapping yet.
		if (skillLvl >= 499 && (skillId == 30002 || skillId == 30003)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GATHER_CBT_CAP());
			return;
		}

		final int price = cost.get(skillLvl);
		final long kinah = player.getInventory().getKinah();
		final int skillLevel = skillLvl;
		RequestResponseHandler responseHandler = new RequestResponseHandler(npc) {

			@Override
			public void acceptRequest(Creature requester, Player responder) {
				if (price < kinah && responder.getInventory().tryDecreaseKinah(price, ItemUpdateType.DEC_KINAH_LEARN)) {
					PlayerSkillList skillList = responder.getSkillList();
					skillList.addSkill(responder, skillId, skillLevel + 1);
				} else {
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
				}
			}

			@Override
			public void denyRequest(Creature requester, Player responder) {
				// do nothing
			}
		};

		boolean result = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_CRAFT_ADDSKILL_CONFIRM, responseHandler);
		if (result) {
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_CRAFT_ADDSKILL_CONFIRM, 0, 0,
				new DescriptionId(DataManager.SKILL_DATA.getSkillTemplate(skillId).getNameId()), String.valueOf(price)));
		}
	}

	/**
	 * check if skillId is crafting skill or not
	 * 
	 * @param skillId
	 * @return true or false
	 */
	public boolean isCraftingSkill(int skillId) {
		Iterator<Integer> it = craftingSkillIds.iterator();
		while (it.hasNext()) {
			if (it.next() == skillId)
				return true;
		}
		return false;
	}

	/**
	 * Get total experted crafting skills
	 * 
	 * @param Player
	 * @return total number of experted crafting skills
	 */
	public int getTotalExpertCraftingSkills(Player player) {
		int mastered = 0;

		Iterator<Integer> it = craftingSkillIds.iterator();
		while (it.hasNext()) {
			int skillId = it.next();
			int skillLvl = 0;
			if (player.getSkillList().isSkillPresent(skillId)) {
				skillLvl = player.getSkillList().getSkillLevel(skillId);
				if (skillLvl > 399 && skillLvl <= 499)
					mastered++;
			}
		}
		return mastered;
	}

	/**
	 * Get total mastered crafting skills
	 * 
	 * @param Player
	 * @return total number of mastered crafting skills
	 */
	public int getTotalMasterCraftingSkills(Player player) {
		int mastered = 0;

		Iterator<Integer> it = craftingSkillIds.iterator();
		while (it.hasNext()) {
			int skillId = it.next();
			int skillLvl = 0;
			if (player.getSkillList().isSkillPresent(skillId)) {
				skillLvl = player.getSkillList().getSkillLevel(skillId);
				if (skillLvl > 499)
					mastered++;
			}
		}

		return mastered;
	}

	/**
	 * Check if player can learn more expert crafting skill or not (max is 2)
	 * 
	 * @param Player
	 * @return true or false
	 */
	public boolean canLearnMoreExpertCraftingSkill(Player player) {
		if (getTotalExpertCraftingSkills(player) + getTotalMasterCraftingSkills(player) < CraftConfig.MAX_EXPERT_CRAFTING_SKILLS) {
			return true;
		} else {
			PacketSendUtility.sendMessage(player, "You can only have " + CraftConfig.MAX_EXPERT_CRAFTING_SKILLS + " Expert crafting skills.");
			return false;
		}
	}

	/**
	 * Check if player can learn more master crafting skill or not (max is 1)
	 * 
	 * @param Player
	 * @return true or false
	 */
	public boolean canLearnMoreMasterCraftingSkill(Player player) {
		if (getTotalMasterCraftingSkills(player) < CraftConfig.MAX_MASTER_CRAFTING_SKILLS) {
			return true;
		} else {
			PacketSendUtility.sendMessage(player, "You can only have " + CraftConfig.MAX_MASTER_CRAFTING_SKILLS + " Master crafting skill.");
			return false;
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		private static final CraftSkillUpdateService instance = new CraftSkillUpdateService();
	}
}
