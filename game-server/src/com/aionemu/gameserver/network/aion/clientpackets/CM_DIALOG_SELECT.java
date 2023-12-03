package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.ClassChangeService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author KKnD , orz, avol, Pad
 */
public class CM_DIALOG_SELECT extends AionClientPacket {

	/**
	 * Target object id that client wants to TALK WITH or 0 if wants to unselect
	 */
	private int targetObjectId;
	private int dialogActionId;
	private int extendedRewardIndex;
	private int lastPage;
	private int questId;
	@SuppressWarnings("unused")
	private int unk;

	public CM_DIALOG_SELECT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
		dialogActionId = readUH();
		extendedRewardIndex = readUH();
		lastPage = readUH();
		questId = readD();
		unk = readUH(); // unk 4.7
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isProtectionActive())
			player.getController().stopProtectionActiveTask();

		if (player.isTrading())
			return;

		String dialogActionName = nameOf(dialogActionId);
		if (player.hasAccess(AdminConfig.DIALOG_INFO)) {
			PacketSendUtility.sendMessage(player, "Quest ID: " + questId + ", Dialog Action: " + dialogActionName + " (ID: " + dialogActionId + ")");
		}
		if (dialogActionName == null) {
			LoggerFactory.getLogger(CM_DIALOG_SELECT.class)
				.warn("Received unknown dialog action id " + dialogActionId + " (quest " + questId + ") from " + player);
			return;
		}

		if (targetObjectId == 0 || targetObjectId == player.getObjectId()) {
			QuestTemplate questTemplate = DataManager.QUEST_DATA.getQuestById(questId);
			if (questTemplate == null)
				return;

			QuestEnv env = new QuestEnv(null, player, questId, dialogActionId);
			if (questTemplate.isCanReport()) {
				switch (dialogActionId) {
					case SELECTED_QUEST_AUTO_REWARD:
					case SELECTED_QUEST_AUTO_REWARD1:
					case SELECTED_QUEST_AUTO_REWARD2:
					case SELECTED_QUEST_AUTO_REWARD3:
					case SELECTED_QUEST_AUTO_REWARD4:
					case SELECTED_QUEST_AUTO_REWARD5:
					case SELECTED_QUEST_AUTO_REWARD6:
					case SELECTED_QUEST_AUTO_REWARD7:
					case SELECTED_QUEST_AUTO_REWARD8:
					case SELECTED_QUEST_AUTO_REWARD9:
					case SELECTED_QUEST_AUTO_REWARD10:
					case SELECTED_QUEST_AUTO_REWARD11:
					case SELECTED_QUEST_AUTO_REWARD12:
					case SELECTED_QUEST_AUTO_REWARD13:
					case SELECTED_QUEST_AUTO_REWARD14:
					case SELECTED_QUEST_AUTO_REWARD15:
						QuestService.finishQuest(env);
						return;
				}
			}
			if (QuestEngine.getInstance().onDialog(env))
				return;
			if (CustomConfig.ENABLE_SIMPLE_2NDCLASS && (questId == 1006 || questId == 2008))
				ClassChangeService.changeClassToSelection(player, dialogActionId);
			return;
		}

		if (player.getKnownList().getObject(targetObjectId) instanceof Creature target) {
			target.getController().onDialogSelect(dialogActionId, lastPage, player, questId, extendedRewardIndex);
		}
	}
}
