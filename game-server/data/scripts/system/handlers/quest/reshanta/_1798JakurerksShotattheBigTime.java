package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Hilgert
 */
public class _1798JakurerksShotattheBigTime extends AbstractQuestHandler {

	public _1798JakurerksShotattheBigTime() {
		super(1798);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(279007).addOnQuestStart(questId);
		qe.registerQuestNpc(279007).addOnTalkEvent(questId);
		qe.registerQuestNpc(263568).addOnTalkEvent(questId);
		qe.registerQuestNpc(263266).addOnTalkEvent(questId);
		qe.registerQuestNpc(264768).addOnTalkEvent(questId);
		qe.registerQuestNpc(271053).addOnTalkEvent(questId);
		qe.registerQuestNpc(266553).addOnTalkEvent(questId);
		qe.registerQuestNpc(270151).addOnTalkEvent(questId);
		qe.registerQuestNpc(269251).addOnTalkEvent(questId);
		qe.registerQuestNpc(268051).addOnTalkEvent(questId);
		qe.registerQuestNpc(260235).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (targetId == 279007) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD)
				return sendQuestEndDialog(env);
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 263568) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == SETPRO1) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 263266) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO2) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 264768) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO3) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 271053) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2035);
				else if (env.getDialogActionId() == SETPRO4) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 266553) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SETPRO5) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 270151) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2716);
				else if (env.getDialogActionId() == SETPRO6) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 269251) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 3057);
				else if (env.getDialogActionId() == SETPRO7) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 268051) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 3398);
				else if (env.getDialogActionId() == SETPRO8) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 260235) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 3740);
				else if (env.getDialogActionId() == SET_SUCCEED) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			}
		}
		return false;
	}
}
