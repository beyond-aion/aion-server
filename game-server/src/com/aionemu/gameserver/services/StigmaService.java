package com.aionemu.gameserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.templates.item.RequireSkill;
import com.aionemu.gameserver.model.templates.item.Stigma;
import com.aionemu.gameserver.model.templates.item.Stigma.StigmaSkill;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.skillengine.model.SkillLearnTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 * @modified cura
 */
public class StigmaService {

	private static final Logger log = LoggerFactory.getLogger(StigmaService.class);

	public static boolean extendAdvancedStigmaSlots(Player player) {
		int newAdvancedSlotSize = player.getCommonData().getAdvancedStigmaSlotSize() + 1;
		if (newAdvancedSlotSize <= 6) { // maximum
			player.getCommonData().setAdvancedStigmaSlotSize(newAdvancedSlotSize);
			PacketSendUtility.sendPacket(player, SM_CUBE_UPDATE.stigmaSlots(player.getCommonData().getAdvancedStigmaSlotSize()));
			return true;
		}
		return false;
	}

	private static String clearName(String itemName) {
		String[] splits = itemName.split(" ");
		String newName = "";
		for (int i = 0; i < (splits.length - 1); i++) {
			newName += splits[i];
		}
		return newName;
	}

	/**
	 * @param player
	 * @param resultItem
	 * @param slot
	 * @return
	 */
	public static boolean notifyEquipAction(Player player, Item resultItem, long slot) {
		if (resultItem.getItemTemplate().isStigma()) {
			Stigma stigmaInfo = resultItem.getItemTemplate().getStigma();

			if (stigmaInfo == null) {
				log.warn("Stigma info missing for item: " + resultItem.getItemTemplate().getTemplateId());
				return false;
			}
			String rsultItemName = clearName(resultItem.getItemName());
			if (ItemSlot.isRegularStigma(slot)) {
				boolean replace = false;
				for (Item i : player.getEquipment().getEquippedItemsRegularStigma()) {
					if (i.getEquipmentSlot() == slot) {
						if (!clearName(i.getItemName()).equals(rsultItemName)) {
							return false;
						}
						if (!notifyUnequipAction(player, i, true)) {
							return false;
						}
						replace = true;
						break;
					}
				}
				// check the number of stigma wearing
				if (!replace && getPossibleStigmaCount(player) <= player.getEquipment().getEquippedItemsRegularStigma().size()) {
					AuditLogger.info(player, "Possible client hack stigma count big :O");
					return false;
				}

			} else if (ItemSlot.isAdvancedStigma(slot)) {
				boolean replace = false;
				for (Item i : player.getEquipment().getEquippedItemsAdvencedStigma()) {
					if (i.getEquipmentSlot() == slot) {
						if (!clearName(i.getItemName()).equals(rsultItemName)) {
							return false;
						}
						if (!notifyUnequipAction(player, i, true)) {
							return false;
						}
						replace = true;
						break;
					}
				}
				// check the number of advanced stigma wearing
				if (!replace && getPossibleAdvencedStigmaCount(player) <= player.getEquipment().getEquippedItemsAdvencedStigma().size()) {
					AuditLogger.info(player, "Possible client hack advanced stigma count big :O");
					return false;
				}
			}

			if (resultItem.getItemTemplate().isClassSpecific(player.getCommonData().getPlayerClass()) == false) {
				AuditLogger.info(player, "Possible client hack not valid for class.");
				return false;
			}

			int shardCount = stigmaInfo.getShard();
			if (player.getInventory().getItemCountByItemId(141000001) < shardCount) {
				AuditLogger.info(player, "Possible client hack stigma shard count low.");
				return false;
			}
			int neededSkillsCount = stigmaInfo.getRequireSkill().size();
			for (RequireSkill rs : stigmaInfo.getRequireSkill()) {
				for (int id : rs.getSkillIds()) {
					if (player.getSkillList().isSkillPresent(id)) {
						neededSkillsCount--;
						break;
					}
				}
			}
			if (neededSkillsCount != 0) {
				AuditLogger.info(player, "Possible client hack advenced stigma skill.");
				return false;
			}

			if (!player.getInventory().decreaseByItemId(141000001, shardCount))
				return false;

			for (StigmaSkill sSkill : stigmaInfo.getSkills())
				player.getSkillList().addTemporarySkill(player, sSkill.getSkillId(), sSkill.getSkillLvl());
		}
		return true;
	}

	/**
	 * @param player
	 * @param resultItem
	 * @return
	 */
	public static boolean notifyUnequipAction(Player player, Item resultItem, boolean replace) {
		if (resultItem.getItemTemplate().isStigma()) {
			Stigma stigmaInfo = resultItem.getItemTemplate().getStigma();
			int itemId = resultItem.getItemId();
			Equipment equipment = player.getEquipment();
			if (itemId == 140000007 || itemId == 140000005) {
				if (equipment.hasDualWeaponEquipped(ItemSlot.LEFT_HAND)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_STIGMA_CANNT_UNEQUIP_STONE_FIRST_UNEQUIP_CURRENT_EQUIPPED_ITEM);
					return false;
				}
			}
			if (!replace) {
				for (Item item : player.getEquipment().getEquippedItemsAllStigma()) {
					Stigma si = item.getItemTemplate().getStigma();
					if (resultItem == item || si == null)
						continue;

					for (StigmaSkill sSkill : stigmaInfo.getSkills()) {
						for (RequireSkill rs : si.getRequireSkill()) {
							if (rs.getSkillIds().contains(sSkill.getSkillId())) {
								PacketSendUtility.sendPacket(player,
									SM_SYSTEM_MESSAGE.STR_STIGMA_CANNT_UNEQUIP_STONE_OTHER_STONE_NEED_ITS_SKILL(item.getNameId(), resultItem.getNameId()));
								return false;
							}
						}
					}
				}
			}
			for (StigmaSkill sSkill : stigmaInfo.getSkills()) {
				int nameId = DataManager.SKILL_DATA.getSkillTemplate(sSkill.getSkillId()).getNameId();
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_STIGMA_YOU_CANNOT_USE_THIS_SKILL_AFTER_UNEQUIP_STIGMA_STONE(nameId));
				SkillLearnService.removeSkill(player, sSkill.getSkillId());
			}
		}
		return true;
	}

	/**
	 * @param player
	 */
	public static void onPlayerLogin(Player player) {
		if (player.havePermission(MembershipConfig.STIGMA_AUTOLEARN)) {
			for (int level = 20; level <= player.getLevel(); level++) {
				for (SkillLearnTemplate template : DataManager.SKILL_TREE_DATA.getTemplatesFor(player.getPlayerClass(), level, player.getRace())) {
					if (template.isStigma())
						player.getSkillList().addTemporarySkill(player, template.getSkillId(), template.getSkillLevel());
				}
			}
			return;
		}

		for (Item item : player.getEquipment().getEquippedItemsAllStigma()) {
			if (!item.getItemTemplate().isStigma()) {
				log.warn("Stigma info missing for item: " + item.getItemId());
				player.getEquipment().unEquipItem(item.getObjectId(), 0);
				continue;
			}

			if (!isPossibleEquippedStigma(player, item)) {
				AuditLogger.info(player, "Possible client hack stigma count big :O");
				player.getEquipment().unEquipItem(item.getObjectId(), 0);
				continue;
			}

			if (!item.getItemTemplate().isClassSpecific(player.getCommonData().getPlayerClass())) {
				AuditLogger.info(player, "Possible client hack not valid for class.");
				player.getEquipment().unEquipItem(item.getObjectId(), 0);
				continue;
			}

			for (StigmaSkill sSkill : item.getItemTemplate().getStigma().getSkills())
				player.getSkillList().addTemporarySkill(player, sSkill.getSkillId(), sSkill.getSkillLvl());
		}

		for (Item item : player.getEquipment().getEquippedItemsAllStigma()) {
			Stigma stigmaInfo = item.getItemTemplate().getStigma();
			int needSkill = stigmaInfo.getRequireSkill().size();
			for (RequireSkill rs : stigmaInfo.getRequireSkill()) {
				for (int id : rs.getSkillIds()) {
					if (player.getSkillList().isSkillPresent(id)) {
						needSkill--;
						break;
					}
				}
			}
			if (needSkill != 0) {
				AuditLogger.info(player, "Possible client hack advenced stigma skill.");
				player.getEquipment().unEquipItem(item.getObjectId(), 0);
				for (StigmaSkill sSkill : item.getItemTemplate().getStigma().getSkills())
					SkillLearnService.removeSkill(player, sSkill.getSkillId());
			}
		}
	}

	/**
	 * Get the number of available Stigma
	 *
	 * @param player
	 * @return
	 */
	private static int getPossibleStigmaCount(Player player) {
		if (player == null || player.getLevel() < 20)
			return 0;

		if (player.havePermission(MembershipConfig.STIGMA_SLOT_QUEST)) {
			return 6;
		}

		/*
		 * Stigma Quest Elyos: 1929, Asmodians: 2900
		 */
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

		int playerLevel = player.getLevel();

		if (isCompleteQuest) {
			if (playerLevel < 30)
				return 2;
			else if (playerLevel < 40)
				return 3;
			else if (playerLevel < 50)
				return 4;
			else if (playerLevel < 55)
				return 5;
			else
				return 6;
		}
		return 0;
	}

	/**
	 * Get the number of available Advenced Stigma
	 *
	 * @param player
	 * @return
	 */
	private static int getPossibleAdvencedStigmaCount(Player player) {
		if (player == null || player.getLevel() < 45)
			return 0;

		if (player.havePermission(MembershipConfig.STIGMA_SLOT_QUEST)) {
			return 6;
		}

		/*
		 * Advenced Stigma Quest 1st - Elyos: 3930, Asmodians: 4934 2nd - Elyos: 3931, Asmodians: 4935 3rd- Elyos: 3932, Asmodians: 4936 4th - Elyos:
		 * 11049, Asmodians: 21049 5th - Elyos: 30217, Asmodians: 30317
		 */
		if (player.getRace() == Race.ELYOS) {
			// Check whether Stigma Quests
			if (!player.isCompleteQuest(1929))
				return 0;
			if (player.isCompleteQuest(11550))
				return 6;
			else if (player.isCompleteQuest(30217) || player.isCompleteQuest(11276))
				return 5;
			else if (player.isCompleteQuest(11049))
				return 4;
			else if (player.isCompleteQuest(3932))
				return 3;
			else if (player.isCompleteQuest(3931))
				return 2;
			else if (player.isCompleteQuest(3930))
				return 1;
		} else {
			// Check whether Stigma Quests
			if (!player.isCompleteQuest(2900))
				return 0;
			if (player.isCompleteQuest(21550))
				return 6;
			else if (player.isCompleteQuest(30317) || player.isCompleteQuest(21278))
				return 5;
			else if (player.isCompleteQuest(21049))
				return 4;
			else if (player.isCompleteQuest(4936))
				return 3;
			else if (player.isCompleteQuest(4935))
				return 2;
			else if (player.isCompleteQuest(4934))
				return 1;
		}
		return 0;
	}

	/**
	 * Stigma is a worn check available slots
	 *
	 * @param player
	 * @param item
	 * @return
	 */
	private static boolean isPossibleEquippedStigma(Player player, Item item) {
		if (player == null || (item == null || !item.getItemTemplate().isStigma()))
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
				} else if (stigmaCount == 3) {
					if (itemSlotToEquip == ItemSlot.STIGMA1.getSlotIdMask() || itemSlotToEquip == ItemSlot.STIGMA2.getSlotIdMask()
						|| itemSlotToEquip == ItemSlot.STIGMA3.getSlotIdMask())
						return true;
				} else if (stigmaCount == 4) {
					if (itemSlotToEquip == ItemSlot.STIGMA1.getSlotIdMask() || itemSlotToEquip == ItemSlot.STIGMA2.getSlotIdMask()
						|| itemSlotToEquip == ItemSlot.STIGMA3.getSlotIdMask() || itemSlotToEquip == ItemSlot.STIGMA4.getSlotIdMask())
						return true;
				} else if (stigmaCount == 5) {
					if (itemSlotToEquip == ItemSlot.STIGMA1.getSlotIdMask() || itemSlotToEquip == ItemSlot.STIGMA2.getSlotIdMask()
						|| itemSlotToEquip == ItemSlot.STIGMA3.getSlotIdMask() || itemSlotToEquip == ItemSlot.STIGMA4.getSlotIdMask()
						|| itemSlotToEquip == ItemSlot.STIGMA5.getSlotIdMask())
						return true;
				} else if (stigmaCount == 6) {
					return true;
				}
			}
		}
		// Advenced Stigma
		else if (ItemSlot.isAdvancedStigma(itemSlotToEquip)) {
			int advStigmaCount = getPossibleAdvencedStigmaCount(player);

			if (advStigmaCount > 0) {
				if (advStigmaCount == 1) {
					if (itemSlotToEquip == ItemSlot.ADV_STIGMA1.getSlotIdMask())
						return true;
				} else if (advStigmaCount == 2) {
					if (itemSlotToEquip == ItemSlot.ADV_STIGMA1.getSlotIdMask() || itemSlotToEquip == ItemSlot.ADV_STIGMA2.getSlotIdMask())
						return true;
				} else if (advStigmaCount == 3) {
					if (itemSlotToEquip == ItemSlot.ADV_STIGMA1.getSlotIdMask() || itemSlotToEquip == ItemSlot.ADV_STIGMA2.getSlotIdMask()
						|| itemSlotToEquip == ItemSlot.ADV_STIGMA3.getSlotIdMask())
						return true;
				} else if (advStigmaCount == 4) {
					if (itemSlotToEquip == ItemSlot.ADV_STIGMA1.getSlotIdMask() || itemSlotToEquip == ItemSlot.ADV_STIGMA2.getSlotIdMask()
						|| itemSlotToEquip == ItemSlot.ADV_STIGMA3.getSlotIdMask() || itemSlotToEquip == ItemSlot.ADV_STIGMA4.getSlotIdMask())
						return true;
				} else if (advStigmaCount == 5) {
					if (itemSlotToEquip == ItemSlot.ADV_STIGMA1.getSlotIdMask() || itemSlotToEquip == ItemSlot.ADV_STIGMA2.getSlotIdMask()
						|| itemSlotToEquip == ItemSlot.ADV_STIGMA3.getSlotIdMask() || itemSlotToEquip == ItemSlot.ADV_STIGMA4.getSlotIdMask()
						|| itemSlotToEquip == ItemSlot.ADV_STIGMA5.getSlotIdMask())
						return true;
				} else if (advStigmaCount == 6) {
					return true;
				}
			}
		}
		return false;
	}

}
