package com.aionemu.gameserver.services.craft;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.model.craft.Profession;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, sphinx, Imaginary, Pad
 */
public class CraftSkillUpdateService {

	private static final Logger log = LoggerFactory.getLogger(CraftSkillUpdateService.class);

	private static final Map<Integer, Profession> professionByNpc = new HashMap<>();

	public static CraftSkillUpdateService getInstance() {
		return SingletonHolder.instance;
	}

	private CraftSkillUpdateService() {
		// Asmodian
		professionByNpc.put(204096, Profession.ESSENCETAPPING);
		professionByNpc.put(830150, Profession.ESSENCETAPPING);
		professionByNpc.put(204257, Profession.AETHERTAPPING);
		professionByNpc.put(830148, Profession.AETHERTAPPING);

		professionByNpc.put(204100, Profession.COOKING);
		professionByNpc.put(830142, Profession.COOKING);
		professionByNpc.put(204104, Profession.WEAPONSMITHING);
		professionByNpc.put(830146, Profession.WEAPONSMITHING);
		professionByNpc.put(204106, Profession.ARMORSMITHING);
		professionByNpc.put(830144, Profession.ARMORSMITHING);
		professionByNpc.put(204110, Profession.TAILORING);
		professionByNpc.put(830136, Profession.TAILORING);
		professionByNpc.put(204102, Profession.ALCHEMY);
		professionByNpc.put(830138, Profession.ALCHEMY);
		professionByNpc.put(204108, Profession.HANDICRAFTING);
		professionByNpc.put(830140, Profession.HANDICRAFTING);
		professionByNpc.put(798452, Profession.CONSTRUCTION);
		professionByNpc.put(798456, Profession.CONSTRUCTION);

		// Elyos
		professionByNpc.put(203780, Profession.ESSENCETAPPING);
		professionByNpc.put(830066, Profession.ESSENCETAPPING);
		professionByNpc.put(203782, Profession.AETHERTAPPING);
		professionByNpc.put(830064, Profession.AETHERTAPPING);

		professionByNpc.put(203784, Profession.COOKING);
		professionByNpc.put(830058, Profession.COOKING);
		professionByNpc.put(203788, Profession.WEAPONSMITHING);
		professionByNpc.put(830062, Profession.WEAPONSMITHING);
		professionByNpc.put(203790, Profession.ARMORSMITHING);
		professionByNpc.put(830060, Profession.ARMORSMITHING);
		professionByNpc.put(203793, Profession.TAILORING);
		professionByNpc.put(830052, Profession.TAILORING);
		professionByNpc.put(203786, Profession.ALCHEMY);
		professionByNpc.put(830054, Profession.ALCHEMY);
		professionByNpc.put(203792, Profession.HANDICRAFTING);
		professionByNpc.put(830056, Profession.HANDICRAFTING);
		professionByNpc.put(798450, Profession.CONSTRUCTION);
		professionByNpc.put(798454, Profession.CONSTRUCTION);

		log.info("CraftSkillUpdateService: Initialized.");
	}

	public Profession getProfessionByNpc(Npc npc) {
		return professionByNpc.get(npc.getNpcId());
	}

	public void learnSkill(Player player, Npc npc) {
		if (player.getLevel() < 10)
			return;
		Profession profession = professionByNpc.get(npc.getNpcId());
		if (profession == null)
			return;
		int skillId = profession.getSkillId();
		if (skillId == 0)
			return;

		PlayerSkillList skillList = player.getSkillList();
		int skillLevel = skillList.isSkillPresent(skillId) ? skillList.getSkillLevel(skillId) : 0;
		Integer price = profession.getUpgradeCost(skillLevel);
		if (price == null) {
			if (skillLevel > profession.getMaxUpgradableLevel())
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DONT_RANK_UP_GATHERING());
			else if (skillLevel == 399) // expert is granted by quest
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CRAFT_CANT_EXTEND_MONEY());
			else if (skillLevel == 499) // master is granted by quest
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CRAFT_CANT_EXTEND_GRAND_MASTER());
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DONT_RANK_UP());
			return;
		}

		RequestResponseHandler<Npc> responseHandler = new RequestResponseHandler<Npc>(npc) {

			@Override
			public void acceptRequest(Npc requester, Player responder) {
				if (responder.getInventory().tryDecreaseKinah(price, ItemUpdateType.DEC_KINAH_LEARN)) {
					PlayerSkillList skillList = responder.getSkillList();
					skillList.addSkill(responder, skillId, skillLevel + 1);
				} else {
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
				}
			}

		};

		if (player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_CRAFT_ADDSKILL_CONFIRM, responseHandler)) {
			String professionName = skillLevel == 0 ? profession.getClientName() : profession.getClientName(skillLevel + 1);
			PacketSendUtility.sendPacket(player,
				new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_CRAFT_ADDSKILL_CONFIRM, 0, 0, professionName, String.valueOf(price)));
		}
	}

	public int getTotalExpertCraftingSkills(Player player) {
		int mastered = 0;

		for (Profession profession : Profession.values()) {
			if (profession.isCrafting() && player.getSkillList().isSkillPresent(profession.getSkillId())) {
				int skillLvl = player.getSkillList().getSkillLevel(profession.getSkillId());
				if (skillLvl > 399 && skillLvl <= 499)
					mastered++;
			}
		}
		return mastered;
	}

	/**
	 * @return total number of mastered crafting skills
	 */
	public int getTotalMasterCraftingSkills(Player player) {
		int mastered = 0;

		for (Profession profession : Profession.values()) {
			if (profession.isCrafting() && player.getSkillList().isSkillPresent(profession.getSkillId())) {
				int skillLvl = player.getSkillList().getSkillLevel(profession.getSkillId());
				if (skillLvl > 499)
					mastered++;
			}
		}

		return mastered;
	}

	public boolean canLearnMoreExpertCraftingSkill(Player player) {
		if (getTotalExpertCraftingSkills(player) + getTotalMasterCraftingSkills(player) < CraftConfig.MAX_EXPERT_CRAFTING_SKILLS) {
			return true;
		} else {
			PacketSendUtility.sendMessage(player, "You can only be an expert in " + CraftConfig.MAX_EXPERT_CRAFTING_SKILLS + " professions.");
			return false;
		}
	}

	public boolean canLearnMoreMasterCraftingSkill(Player player) {
		if (getTotalMasterCraftingSkills(player) < CraftConfig.MAX_MASTER_CRAFTING_SKILLS) {
			return true;
		} else {
			PacketSendUtility.sendMessage(player, "You can only be a master in " + CraftConfig.MAX_MASTER_CRAFTING_SKILLS + " professions.");
			return false;
		}
	}

	private static class SingletonHolder {

		private static final CraftSkillUpdateService instance = new CraftSkillUpdateService();
	}
}
