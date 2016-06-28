package com.aionemu.gameserver.services;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEVEL_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, sweetkr
 * @reworked Neon
 */
public class ClassChangeService {

	public static void showClassChangeDialog(Player player) {
		PlayerClass playerClass = player.getPlayerClass();
		Race playerRace = player.getRace();
		if (player.getLevel() >= 9 && playerClass.isStartingClass())
			PacketSendUtility.sendPacket(player,
				new SM_DIALOG_WINDOW(0, getClassSelectionDialogId(playerRace, playerClass), playerRace == Race.ELYOS ? 1006 : 2008));
	}

	public static void changeClassToSelection(Player player, int dialogId) {
		setClass(player, getSelectedPlayerClass(player.getRace(), dialogId), true, true);
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0, 0));
	}

	public static void completeAscensionQuest(Player player) {
		int questId = player.getRace() == Race.ELYOS ? 1006 : 2008;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			qs = new QuestState(questId, QuestStatus.COMPLETE);
			player.getQuestStateList().addQuest(questId, qs);
			PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.ADD, qs));
		} else {
			qs.setStatus(QuestStatus.COMPLETE);
		}
		qs.setQuestVar(0);
		qs.setReward(0);
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.UPDATE, qs));
	}

	public static boolean setClass(Player player, PlayerClass newClass) {
		return setClass(player, newClass, true, false);
	}

	public static boolean setClass(Player player, PlayerClass newClass, boolean validate, boolean updateDaevaStatus) {
		if (newClass == null)
			return false;

		if (validate) {
			PlayerClass oldClass = player.getPlayerClass();
			if (!oldClass.isStartingClass()) {
				PacketSendUtility.sendMessage(player, "You already switched class");
				return false;
			}
			byte id = oldClass.getClassId(); // starting class ID +1/+2 equals valid subclass ID
			if (oldClass == newClass || id >= PlayerClass.ALL.getClassId() || newClass.getClassId() <= id || newClass.getClassId() > id + 2) {
				PacketSendUtility.sendMessage(player, "Invalid class chosen");
				return false;
			}
		}

		player.getCommonData().setPlayerClass(newClass);
		player.getController().upgradePlayer();
		PacketSendUtility.broadcastPacket(player, new SM_LEVEL_UPDATE(player.getObjectId(), 4, player.getLevel()), true);
		SkillLearnService.learnNewSkills(player, 9, player.getLevel());

		if (updateDaevaStatus) {
			if (!newClass.isStartingClass()) {
				completeAscensionQuest(player);
				player.getCommonData().updateDaeva();
			} else {
				player.getCommonData().setDaeva(false);
			}
		}
		return true;
	}

	public static int getClassSelectionDialogId(Race playerRace, PlayerClass playerClass) {
		switch (playerClass) {
			case WARRIOR:
				return playerRace == Race.ELYOS ? 2375 : 3057;
			case SCOUT:
				return playerRace == Race.ELYOS ? 2716 : 3398;
			case MAGE:
				return playerRace == Race.ELYOS ? 3057 : 3739;
			case PRIEST:
				return playerRace == Race.ELYOS ? 3398 : 4080;
			case ENGINEER:
				return playerRace == Race.ELYOS ? 3739 : 3569;
			case ARTIST:
				return playerRace == Race.ELYOS ? 4080 : 3910;
			default:
				return 0;
		}
	}

	public static PlayerClass getSelectedPlayerClass(Race race, int dialogId) {
		switch (race) {
			case ELYOS:
				switch (dialogId) {
					case 2376:
						return PlayerClass.GLADIATOR;
					case 2461:
						return PlayerClass.TEMPLAR;
					case 2717:
						return PlayerClass.ASSASSIN;
					case 2802:
						return PlayerClass.RANGER;
					case 3058:
						return PlayerClass.SORCERER;
					case 3143:
						return PlayerClass.SPIRIT_MASTER;
					case 3399:
						return PlayerClass.CLERIC;
					case 3484:
						return PlayerClass.CHANTER;
					case 3825:
						return PlayerClass.RIDER;
					case 3740:
						return PlayerClass.GUNNER;
					case 4081:
						return PlayerClass.BARD;
					default:
						return null;
				}
			case ASMODIANS:
				switch (dialogId) {
					case 3058:
						return PlayerClass.GLADIATOR;
					case 3143:
						return PlayerClass.TEMPLAR;
					case 3399:
						return PlayerClass.ASSASSIN;
					case 3484:
						return PlayerClass.RANGER;
					case 3740:
						return PlayerClass.SORCERER;
					case 3825:
						return PlayerClass.SPIRIT_MASTER;
					case 4081:
						return PlayerClass.CLERIC;
					case 4166:
						return PlayerClass.CHANTER;
					case 3591:
						return PlayerClass.RIDER;
					case 3570:
						return PlayerClass.GUNNER;
					case 3911:
						return PlayerClass.BARD;
					default:
						return null;
				}
			default:
				return null;
		}
	}
}
