package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.Stigma;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.skillengine.model.SkillLearnTemplate;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, cura, Neon
 */
public class StigmaService {

	private static final Logger log = LoggerFactory.getLogger(StigmaService.class);

	public static boolean notifyEquipAction(Player player, Item resultItem, long slot) {
		if (resultItem.getItemTemplate().isStigma()) {
			Stigma stigmaInfo = resultItem.getItemTemplate().getStigma();
			int stigmaLevel = resultItem.getEnchantLevel();
			String stigmaName = resultItem.getItemName().replace(" ", "").toUpperCase();
			boolean replace = false;
			for (Item i : player.getEquipment().getEquippedItemsAllStigma()) {
				if (i.getEquipmentSlot() == slot) {
					if (!stigmaName.equals(i.getItemName().replace(" ", "").toUpperCase()))
						return false;
					removeStigmaSkills(player, i.getItemTemplate().getStigma(), i.getEnchantLevel(), i.getEnchantLevel() > resultItem.getEnchantLevel());
					replace = true;
					break;
				}
			}
			if (!replace) {
				// check the number of stigma wearing
				if (ItemSlot.isRegularStigma(slot) && getPossibleStigmaCount(player) <= player.getEquipment().getEquippedItemsRegularStigma().size()) {
					AuditLogger.log(player, "tried to equip stigma, exceeding the socket limit");
					return false;
				} else if (ItemSlot.isAdvancedStigma(slot)
					&& getPossibleAdvancedStigmaCount(player) <= player.getEquipment().getEquippedItemsAdvancedStigma().size()) {
					AuditLogger.log(player, "tried to equip advanced stigma, exceeding the socket limit");
					return false;
				}
			}

			long kinahcount = 25000;
			// Sets the price for equipping stigma during mission in Space of Destiny [ID: 320070000] and Sliver of darkness [ID: 310070000]
			if ((player.getRace() == Race.ASMODIANS && player.getWorldId() == 320070000)
				|| (player.getRace() == Race.ELYOS && player.getWorldId() == 310070000))
				kinahcount = 1000;
			else if (resultItem.getItemTemplate().getItemQuality().equals(ItemQuality.LEGEND))
				kinahcount = 50000;
			else if (resultItem.getItemTemplate().getItemQuality().equals(ItemQuality.UNIQUE))
				kinahcount = 100000;

			if (!player.getInventory().tryDecreaseKinah(PricesService.getPriceForService(kinahcount, player.getRace()))) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_STIGMA_NOT_ENOUGH_MONEY());
				return false;
			}
			addStigmaSkills(player, stigmaInfo, stigmaLevel);
		}
		return true;
	}

	public static void onPlayerLogin(Player player) {
		if (player.hasPermission(MembershipConfig.STIGMA_AUTOLEARN)) {
			for (int level = 20; level <= player.getLevel(); level++) {
				for (SkillLearnTemplate template : DataManager.SKILL_TREE_DATA.getTemplatesFor(player.getPlayerClass(), level, player.getRace())) {
					if (template.isStigma())
						SkillLearnService.learnTemporarySkill(player, template.getSkillId(), template.getSkillLevel());
				}
			}
			return;
		}

		mainLoop:
		for (Item item : player.getEquipment().getEquippedItemsAllStigma()) {
			if (!item.getItemTemplate().isStigma()) {
				player.getEquipment().unEquipItem(item.getObjectId(), false);
				log.warn("Unequipped stigma: " + item.getItemId() + ", stigma info missing for item (possibly pre-4.8 stigma)");
				continue;
			}

			if (!isPossibleEquippedStigma(player, item)) {
				player.getEquipment().unEquipItem(item.getObjectId(), false);
				AuditLogger.log(player, "had more equipped stigmas on login than allowed");
				continue;
			}

			if (!item.getItemTemplate().isClassSpecific(player.getPlayerClass())) {
				player.getEquipment().unEquipItem(item.getObjectId(), false);
				AuditLogger.log(player, "had an equipped stigma on login which was not for his class");
				continue;
			}

			// check for double stigmas equipped into the same slot
			for (Item checkStigma : player.getEquipment().getEquippedItemsAllStigma()) {
				if (checkStigma.getEquipmentSlot() == item.getEquipmentSlot() && checkStigma.getItemId() != item.getItemId()) {
					player.getEquipment().unEquipItem(item.getObjectId(), false);
					AuditLogger.log(player, "had two stigmas equipped in the same slot on login");
					continue mainLoop;
				}
			}

			addStigmaSkills(player, item.getItemTemplate().getStigma(), item.getEnchantLevel());
		}

		addLinkedStigmaSkills(player);
	}

	public static void removeLinkedStigmaSkills(Player player) {
		List<PlayerSkillEntry> linkedStigmaSkills = new ArrayList<>();
		while (true) { // remove all linked stigma skills (can be more than one if stigma auto learning is enabled)
			String stack = null;
			linkedStigmaSkills.clear();
			for (PlayerSkillEntry skill : player.getSkillList().getAllSkills()) {
				if (skill.isLinkedStigmaSkill()) {
					SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skill.getSkillId());
					if (stack == null)
						stack = skillTemplate.getStack();
					if (skillTemplate.getStack().equalsIgnoreCase(stack))
						linkedStigmaSkills.add(skill);
					if (stack.equalsIgnoreCase("NONE"))
						break;
				}
			}
			if (linkedStigmaSkills.isEmpty())
				break;
			String firstSkillL10n = null, secondSkillL10n = null;
			int skillLevel = 0;
			for (int i = 0; i < linkedStigmaSkills.size(); i++) {
				PlayerSkillEntry skillEntry = linkedStigmaSkills.get(i);
				SkillLearnService.removeSkill(player, skillEntry.getSkillId());
				if (i == 0) {
					firstSkillL10n = DataManager.SKILL_DATA.getSkillTemplate(skillEntry.getSkillId()).getL10n();
					skillLevel = skillEntry.getSkillLevel();
				} else if (i == 1)
					secondSkillL10n = DataManager.SKILL_DATA.getSkillTemplate(skillEntry.getSkillId()).getL10n();
			}
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_STIGMA_DELETE_HIDDEN_SKILL(firstSkillL10n, skillLevel, secondSkillL10n));
		}
	}

	public static void addLinkedStigmaSkills(Player player) {
		List<Item> stigmas = player.getEquipment().getEquippedItemsAllStigma();
		if (stigmas.size() < 6)
			return;

		for (Item stigma : stigmas) {
			if (!stigma.isStigmaChargeable())
				return;
		}

		int skillId = getLinkedStigmaLearnSkill(player);
		if (skillId > 0) {
			// linked stigma level is the lowest enchant level of all equipped stigmas
			int linkedStigmaSkillLevel = stigmas.stream().min((i1, i2) -> i1.getEnchantLevel() - i2.getEnchantLevel()).get().getEnchantLevel() + 1;
			for (SkillLearnTemplate skill : DataManager.SKILL_TREE_DATA.getSkillsForSkill(skillId, player.getPlayerClass(), player.getRace(),
				player.getLevel()))
				SkillLearnService.learnTemporarySkill(player, skill.getSkillId(), linkedStigmaSkillLevel);
		}
	}

	private static int getLinkedStigmaLearnSkill(Player player) {
		// references: http://aion.mouseclic.com/beta/stigma.php
		boolean isEly = player.getRace() == Race.ELYOS;
		switch (player.getPlayerClass()) {
			case GLADIATOR:
				if (isEquipped(player, 140001118) && isEquipped(player, 2, 140001103, 140001104, 140001105))
					return 731; // Wind Lance
				if (isEquipped(player, 140001119) && isEquipped(player, 2, 140001106, 140001107, 140001108))
					return 643; // Unraveling Assault
				return !isEly ? 661 : 662; // Battle Banner
			case TEMPLAR:
				if (isEquipped(player, 140001134) && isEquipped(player, 2, 140001120, 140001122, 140001125))
					return 2921; // Invigorating Strike
				if (isEquipped(player, 140001135) && isEquipped(player, 2, 140001121, 140001123, 140001124))
					return 2918; // Shield of Vengeance
				return 2917; // Eternal Denial
			case ASSASSIN:
				if (isEquipped(player, 140001151) && isEquipped(player, 2, 140001136, 140001137, 140001140))
					return 3241; // Fangdrop Stab
				if (isEquipped(player, 140001152) && isEquipped(player, 2, 140001138, 140001139, 140001141))
					return 3238; // Shimmerbomb
				return 3244; // Explosive Rebranding
			case RANGER:
				if (isEquipped(player, 140001172) && isEquipped(player, 2, 140001153, 140001155, 140001157))
					return 1008; // Ripthread Shot
				if (isEquipped(player, 140001173) && isEquipped(player, 2, 140001154, 140001156, 140001158))
					return 938; // Night Haze
				return isEly ? 1065 : 1064; // Staggering Trap
			case SORCERER:
				if (isEquipped(player, 140001191) && isEquipped(player, 2, 140001174, 140001178, 140001181))
					return 1342; // Slumberswept Wind
				if (isEquipped(player, 140001192) && isEquipped(player, 2, 140001176, 140001177, isEly ? 140001184 : 140001185))
					return 1542; // Aetherblaze
				return 1420; // Repulsion Field
			case SPIRIT_MASTER:
				if (isEquipped(player, 140001209) && isEquipped(player, 2, 140001193, 140001194, 140001195))
					return 3543; // Spirit's Empowerment
				if (isEquipped(player, 140001210) && isEquipped(player, 2, 140001196, isEly ? 140001197 : 140001198, 140001199))
					return 3549; // Command: Absorb Wounds
				return 3851; // Blood Funnel
			case CLERIC:
				if (isEquipped(player, 140001245) && isEquipped(player, 2, 140001228, 140001229, isEly ? 140001230 : 140001231))
					return 4169; // Judge's Edict
				if (isEquipped(player, 140001246) && isEquipped(player, 2, 140001232, 140001233, isEly ? 140001234 : 140001235))
					return 3934; // Restoration Relief
				return isEly ? 3906 : 3911; // Summon Vexing Energy
			case CHANTER:
				if (isEquipped(player, 140001226) && isEquipped(player, 2, 140001211, 140001212, 140001213))
					return 1909; // Word of Instigation
				if (isEquipped(player, 140001227) && isEquipped(player, 2, 140001214, 140001215, 140001216))
					return 1903; // Resonant Strike
				return 1906; // Debilitating Incantation
			case RIDER:
				if (isEquipped(player, 140001279) && isEquipped(player, 2, 140001264, 140001265, 140001269))
					return 2858; // Explosive Exhaust
				if (isEquipped(player, 140001280) && isEquipped(player, 2, 140001266, 140001267, 140001268))
					return 2863; // Powerspike Trigger
				return 2851; // Nerve Pulse
			case GUNNER:
				if (isEquipped(player, 140001262) && isEquipped(player, 2, 140001247, 140001248, 140001249))
					return 2370; // Pursuit Stance
				if (isEquipped(player, 140001263) && isEquipped(player, 2, 140001250, 140001251, 140001252))
					return 2377; // Sequential Fire
				return 2382; // Pulverizer Cannon
			case BARD:
				if (isEquipped(player, 140001296) && isEquipped(player, 2, 140001281, 140001282, 140001284))
					return 4480; // Blazing Requiem
				if (isEquipped(player, 140001297) && isEquipped(player, 2, 140001283, 140001285, 140001286))
					return 4483; // Purging Paean
				return 4566; // Delusional Dirge
		}
		return 0;
	}

	public static boolean isEquipped(Player player, int itemId) {
		if (player.getEquipment().getEquippedItemsByItemId(itemId) != null)
			return player.getEquipment().getEquippedItemsByItemId(itemId).size() > 0;
		return false;
	}

	public static boolean isEquipped(Player player, int neededCount, int... itemIds) {
		int equippedCount = 0;
		for (int itemId : itemIds)
			if (isEquipped(player, itemId))
				equippedCount += 1;
		return equippedCount == neededCount;
	}

	private static int getPossibleStigmaCount(Player player) {
		if (player.hasPermission(MembershipConfig.STIGMA_SLOT_QUEST))
			return 3;
		int playerLevel = player.getLevel();
		boolean isCompleteQuest = isCompleteQuest(player);
		if (isCompleteQuest) {
			if (playerLevel < 30)
				return 1;
			else if (playerLevel < 40)
				return 2;
			else
				return 3;
		}
		return 0;
	}

	private static boolean isCompleteQuest(Player player) {
		// Stigma Quest Elyos: 1929, Asmodians: 2900
		boolean isCompleteQuest = false;

		if (player.getRace() == Race.ELYOS) {
			QuestState qs = player.getQuestStateList().getQuestState(1929);
			if (qs != null)
				isCompleteQuest = player.isCompleteQuest(1929) || (qs.getStatus() == QuestStatus.START && qs.getQuestVars().getQuestVars() == 98);
			else
				isCompleteQuest = player.isCompleteQuest(1929);
		} else {
			QuestState qs = player.getQuestStateList().getQuestState(2900);
			if (qs != null)
				isCompleteQuest = player.isCompleteQuest(2900) || (qs.getStatus() == QuestStatus.START && qs.getQuestVars().getQuestVars() == 99);
			else
				isCompleteQuest = player.isCompleteQuest(2900);
		}
		return isCompleteQuest;
	}

	private static int getPossibleAdvancedStigmaCount(Player player) {
		if (player.hasPermission(MembershipConfig.STIGMA_SLOT_QUEST))
			return 3;
		int playerLevel = player.getLevel();
		boolean isCompleteQuest = isCompleteQuest(player);
		if (isCompleteQuest) {
			if (playerLevel >= 55)
				return 3;
			else if (playerLevel >= 50)
				return 2;
			else if (playerLevel >= 45)
				return 1;
		}
		return 0;
	}

	private static boolean isPossibleEquippedStigma(Player player, Item item) {
		if (!item.getItemTemplate().isStigma())
			return false;

		long itemSlotToEquip = item.getEquipmentSlot();

		// Stigma
		if (ItemSlot.isRegularStigma(itemSlotToEquip)) {
			int stigmaCount = getPossibleStigmaCount(player);
			if (stigmaCount > 0) {
				if (stigmaCount == 1) {
					if (itemSlotToEquip == ItemSlot.STIGMA1.getSlotIdMask())
						return true;
				} else if (stigmaCount == 2) {
					if (itemSlotToEquip == ItemSlot.STIGMA1.getSlotIdMask() || itemSlotToEquip == ItemSlot.STIGMA2.getSlotIdMask())
						return true;
				} else if (stigmaCount == 3)
					return true;
			}
		}
		// Advanced Stigma
		else if (ItemSlot.isAdvancedStigma(itemSlotToEquip)) {
			int advStigmaCount = getPossibleAdvancedStigmaCount(player);
			if (advStigmaCount > 0) {
				if (advStigmaCount == 1) {
					if (itemSlotToEquip == ItemSlot.ADV_STIGMA1.getSlotIdMask())
						return true;
				} else if (advStigmaCount == 2) {
					if (itemSlotToEquip == ItemSlot.ADV_STIGMA1.getSlotIdMask() || itemSlotToEquip == ItemSlot.ADV_STIGMA2.getSlotIdMask())
						return true;
				} else if (advStigmaCount == 3)
					return true;
			}
		}
		return false;
	}

	public static void chargeStigma(Player player, Item stigma, Item chargeStone) {
		Stigma stigmaInfo = stigma.getItemTemplate().getStigma();
		if (stigma.getItemId() != chargeStone.getItemId() || chargeStone.getEnchantLevel() > 0 || stigma.getEnchantLevel() >= 10)
			return;
		if (!stigma.isStigmaChargeable())
			return;

		final boolean isSuccess = Rnd.chance() < Math.max(25, 100 - (stigma.getEnchantLevel() * 10));

		final int parentItemId = stigma.getItemId();
		final int parentObjectId = stigma.getObjectId();
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentObjectId, chargeStone.getObjectId(), parentItemId, 5000, 0, 0), true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(stigma.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentObjectId, chargeStone.getObjectId(), parentItemId, 0, 2, 0), true);
				player.getObserveController().removeObserver(this);
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentObjectId, parentItemId, 0, isSuccess ? 1 : 2, 1), true);
				if (!player.getInventory().decreaseByObjectId(chargeStone.getObjectId(), 1, ItemPacketService.ItemUpdateType.DEC_STIGMA_USE))
					return;
				if (!isSuccess) {
					if (stigma.isEquipped())
						player.getEquipment().unEquipItem(stigma.getObjectId());
					player.getInventory().decreaseByObjectId(stigma.getObjectId(), 1, ItemPacketService.ItemUpdateType.DEC_STIGMA_USE);
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_STIGMA_ENCHANT_FAIL(stigma.getL10n()));
				} else {
					stigma.setEnchantLevel(stigma.getEnchantLevel() + 1);
					if (stigma.isEquipped()) {
						removeStigmaSkills(player, stigmaInfo, stigma.getEnchantLevel() - 1, false);
						addStigmaSkills(player, stigmaInfo, stigma.getEnchantLevel());
					}
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_STIGMA_ENCHANT_SUCCESS(stigma.getL10n()));
					PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, stigma));
					if (stigma.getPersistentState() != PersistentState.DELETED) {
						stigma.setPersistentState(PersistentState.UPDATE_REQUIRED);
						if (stigma.isEquipped())
							player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
						else
							player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
					}
				}
			}

		}, 5000));
	}

	private static void addStigmaSkills(Player player, Stigma stigma, int stigmaLevel) {
		for (String skillGroup : stigma.getGainSkillGroups())
			for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplatesByGroup(skillGroup))
				for (SkillLearnTemplate skill : DataManager.SKILL_TREE_DATA.getTemplatesForSkill(st.getSkillId(), player.getPlayerClass(), player.getRace()))
					if (player.getLevel() >= skill.getMinLevel())
						SkillLearnService.learnTemporarySkill(player, skill.getSkillId(), stigmaLevel + 1);
	}

	public static void removeStigmaSkills(Player player, Stigma stigma, int stigmaLevel, boolean notifyPlayer) {
		List<String> notifiedSkillL10ns = new ArrayList<>();
		for (String skillGroup : stigma.getGainSkillGroups()) {
			for (SkillTemplate st : DataManager.SKILL_DATA.getSkillTemplatesByGroup(skillGroup)) {
				if (notifyPlayer && st.getL10n() != null && !notifiedSkillL10ns.contains(st.getL10n())) {
					notifiedSkillL10ns.add(st.getL10n());
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_STIGMA_YOU_CANNOT_USE_THIS_SKILL_AFTER_UNEQUIP_STIGMA_STONE(st.getL10n()));
				}
				for (SkillLearnTemplate skill : DataManager.SKILL_TREE_DATA.getTemplatesForSkill(st.getSkillId(), player.getPlayerClass(), player.getRace()))
					SkillLearnService.removeSkill(player, skill.getSkillId());
			}
		}
		removeLinkedStigmaSkills(player);
	}
}
