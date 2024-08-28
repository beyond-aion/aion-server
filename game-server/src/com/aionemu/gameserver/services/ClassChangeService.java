package com.aionemu.gameserver.services;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.ActionAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ACTION_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, sweetkr, Neon
 */
public class ClassChangeService {

	public static void showClassChangeDialog(Player player) {
		PlayerClass playerClass = player.getPlayerClass();
		Race playerRace = player.getRace();
		if (player.getLevel() >= 9 && playerClass.isStartingClass())
			PacketSendUtility.sendPacket(player,
				new SM_DIALOG_WINDOW(0, getClassSelectionDialogPageId(playerRace, playerClass), playerRace == Race.ELYOS ? 1006 : 2008));
	}

	public static void changeClassToSelection(Player player, int dialogActionId) {
		setClass(player, getSelectedPlayerClass(player.getRace(), dialogActionId), true, true);
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0)); // close dialog window
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
		qs.setRewardGroup(0);
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
			if (oldClass == newClass || newClass.getClassId() <= id || newClass.getClassId() > id + 2) {
				PacketSendUtility.sendMessage(player, "Invalid class chosen");
				return false;
			}
		}

		player.getCommonData().setPlayerClass(newClass);
		player.getGameStats().updateStatsTemplate();
		player.getController().upgradePlayer();
		PacketSendUtility.broadcastPacket(player, new SM_ACTION_ANIMATION(player.getObjectId(), ActionAnimation.CLASS_CHANGE, player.getLevel()), true);
		PacketSendUtility.broadcastPacket(player, new SM_PLAYER_INFO(player));
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

	public static int getClassSelectionDialogPageId(Race playerRace, PlayerClass playerClass) {
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

	public static PlayerClass getSelectedPlayerClass(Race race, int dialogActionId) {
		switch (race) {
			case ELYOS:
				switch (dialogActionId) {
					case SELECT5_1:
						return PlayerClass.GLADIATOR;
					case SELECT5_2:
						return PlayerClass.TEMPLAR;
					case SELECT6_1:
						return PlayerClass.ASSASSIN;
					case SELECT6_2:
						return PlayerClass.RANGER;
					case SELECT7_1:
						return PlayerClass.SORCERER;
					case SELECT7_2:
						return PlayerClass.SPIRIT_MASTER;
					case SELECT8_1:
						return PlayerClass.CLERIC;
					case SELECT8_2:
						return PlayerClass.CHANTER;
					case SELECT9_1:
						return PlayerClass.GUNNER;
					case SELECT9_2:
						return PlayerClass.RIDER;
					case SELECT10_1:
						return PlayerClass.BARD;
				}
				break;
			case ASMODIANS:
				switch (dialogActionId) {
					case SELECT7_1:
						return PlayerClass.GLADIATOR;
					case SELECT7_2:
						return PlayerClass.TEMPLAR;
					case SELECT8_1:
						return PlayerClass.ASSASSIN;
					case SELECT8_2:
						return PlayerClass.RANGER;
					case SELECT9_1:
						return PlayerClass.SORCERER;
					case SELECT9_2:
						return PlayerClass.SPIRIT_MASTER;
					case SELECT10_1:
						return PlayerClass.CLERIC;
					case SELECT10_2:
						return PlayerClass.CHANTER;
					case SELECT8_3_1:
						return PlayerClass.GUNNER;
					case SELECT8_3_2:
						return PlayerClass.RIDER;
					case SELECT9_3_1:
						return PlayerClass.BARD;
				}
		}
		return null;
	}
}
