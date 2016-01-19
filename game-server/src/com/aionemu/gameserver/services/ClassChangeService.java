package com.aionemu.gameserver.services;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEVEL_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, sweetkr
 */
public class ClassChangeService {

	// TODO dialog enum
	/**
	 * @param player
	 */
	public static void showClassChangeDialog(Player player) {
		if (CustomConfig.ENABLE_SIMPLE_2NDCLASS) {
			PlayerClass playerClass = player.getPlayerClass();
			Race playerRace = player.getRace();
			if (player.getLevel() >= 9 && playerClass.isStartingClass()) {
				if (playerRace == Race.ELYOS) {
					switch (playerClass) {
						case WARRIOR:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 2375, 1006));
							break;
						case SCOUT:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 2716, 1006));
							break;
						case MAGE:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 3057, 1006));
							break;
						case PRIEST:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 3398, 1006));
							break;
						case ENGINEER:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 3739, 1006));
							break;
						case ARTIST:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 4080, 1006));
							break;
					}
				} else if (playerRace == Race.ASMODIANS) {
					switch (playerClass) {
						case WARRIOR:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 3057, 2008));
							break;
						case SCOUT:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 3398, 2008));
							break;
						case MAGE:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 3739, 2008));
							break;
						case PRIEST:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 4080, 2008));
							break;
						case ENGINEER:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 3569, 2008));
							break;
						case ARTIST:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 3910, 2008));
							break;
					}
				}
			}
		}
	}

	/**
	 * @param player
	 * @param dialogId
	 */
	public static void changeClassToSelection(final Player player, final int dialogId) {
		Race playerRace = player.getRace();
		if (CustomConfig.ENABLE_SIMPLE_2NDCLASS) {
			if (playerRace == Race.ELYOS) {
				switch (dialogId) {
					case 2376:
						setClass(player, PlayerClass.getPlayerClassById((byte) 1));
						break;
					case 2461:
						setClass(player, PlayerClass.getPlayerClassById((byte) 2));
						break;
					case 2717:
						setClass(player, PlayerClass.getPlayerClassById((byte) 4));
						break;
					case 2802:
						setClass(player, PlayerClass.getPlayerClassById((byte) 5));
						break;
					case 3058:
						setClass(player, PlayerClass.getPlayerClassById((byte) 7));
						break;
					case 3143:
						setClass(player, PlayerClass.getPlayerClassById((byte) 8));
						break;
					case 3399:
						setClass(player, PlayerClass.getPlayerClassById((byte) 10));
						break;
					case 3484:
						setClass(player, PlayerClass.getPlayerClassById((byte) 11));
						break;
					case 3825:
						setClass(player, PlayerClass.getPlayerClassById((byte) 13));
						break;
					case 3740:
						setClass(player, PlayerClass.getPlayerClassById((byte) 14));
						break;
					case 4081:
						setClass(player, PlayerClass.getPlayerClassById((byte) 16));
						break;

				}
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0, 0));
				completeQuest(player, 1006);

				// Stigma Quests Elyos
				if (player.havePermission(MembershipConfig.STIGMA_SLOT_QUEST)) {
					completeQuest(player, 1929);
				}
			} else if (playerRace == Race.ASMODIANS) {
				switch (dialogId) {
					case 3058:
						setClass(player, PlayerClass.getPlayerClassById((byte) 1));
						break;
					case 3143:
						setClass(player, PlayerClass.getPlayerClassById((byte) 2));
						break;
					case 3399:
						setClass(player, PlayerClass.getPlayerClassById((byte) 4));
						break;
					case 3484:
						setClass(player, PlayerClass.getPlayerClassById((byte) 5));
						break;
					case 3740:
						setClass(player, PlayerClass.getPlayerClassById((byte) 7));
						break;
					case 3825:
						setClass(player, PlayerClass.getPlayerClassById((byte) 8));
						break;
					case 4081:
						setClass(player, PlayerClass.getPlayerClassById((byte) 10));
						break;
					case 4166:
						setClass(player, PlayerClass.getPlayerClassById((byte) 11));
						break;
					case 3591:
						setClass(player, PlayerClass.getPlayerClassById((byte) 13));
						break;
					case 3570:
						setClass(player, PlayerClass.getPlayerClassById((byte) 14));
						break;
					case 3911:
						setClass(player, PlayerClass.getPlayerClassById((byte) 16));
						break;
				}
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0, 0));
				// Optimate @Enomine
				completeQuest(player, 2008);

				// Stigma Quests Asmodians
				if (player.havePermission(MembershipConfig.STIGMA_SLOT_QUEST)) {
					completeQuest(player, 2900);
				}
			}
		}
	}

	public static void completeQuest(Player player, int questId) {
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			player.getQuestStateList().addQuest(questId, new QuestState(questId, QuestStatus.COMPLETE, 0, 0, null, 0, null));
			PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(questId, QuestStatus.COMPLETE.value(), 0, 0));
		} else {
			qs.setStatus(QuestStatus.COMPLETE);
			PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars().getQuestVars(), qs.getFlags()));
		}
	}

	public static boolean setClass(Player player, PlayerClass newClass) {
		return setClass(player, newClass, true, false);
	}

	public static boolean setClass(Player player, PlayerClass newClass, boolean validate, boolean updateDaevaStatus) {
		if (validate) {
			PlayerClass oldClass = player.getPlayerClass();
			if (!oldClass.isStartingClass()) {
				PacketSendUtility.sendMessage(player, "You already switched class");
				return false;
			}
			int id = oldClass.getClassId(); // starting class ID +1/+2 equals valid subclass ID
			if (id > PlayerClass.ALL.getClassId() || newClass.getClassId() <= id || newClass.getClassId() > id + 2) {
				PacketSendUtility.sendMessage(player, "Invalid class chosen");
				return false;
			}
		}

		player.getCommonData().setPlayerClass(newClass);
		player.getController().upgradePlayer();
		PacketSendUtility.broadcastPacket(player, new SM_LEVEL_UPDATE(player.getObjectId(), 4, player.getLevel()), true);
		SkillLearnService.addNewSkills(player);

		if (updateDaevaStatus) {
			if (!newClass.isStartingClass()) {
				completeQuest(player, player.getRace() == Race.ELYOS ? 1006 : 2008);
				player.getCommonData().updateDaeva();
			} else {
				player.getCommonData().setDaeva(false);
			}
		}
		return true;
	}
}
