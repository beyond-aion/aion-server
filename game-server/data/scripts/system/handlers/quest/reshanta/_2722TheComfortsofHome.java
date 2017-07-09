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

public class _2722TheComfortsofHome extends AbstractQuestHandler {

	public _2722TheComfortsofHome() {
		super(2722);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(278047).addOnQuestStart(questId);
		qe.registerQuestNpc(278056).addOnTalkEvent(questId);
		qe.registerQuestNpc(278126).addOnTalkEvent(questId);
		qe.registerQuestNpc(278043).addOnTalkEvent(questId);
		qe.registerQuestNpc(278032).addOnTalkEvent(questId);
		qe.registerQuestNpc(278037).addOnTalkEvent(questId);
		qe.registerQuestNpc(278040).addOnTalkEvent(questId);
		qe.registerQuestNpc(278068).addOnTalkEvent(questId);
		qe.registerQuestNpc(278066).addOnTalkEvent(questId);
		qe.registerQuestNpc(278047).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 278047) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 278056) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == SETPRO1) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 278126) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO2) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 278043) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO3) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 278032) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2034);
				else if (env.getDialogActionId() == SETPRO4) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 278037) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SETPRO5) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 278040) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2716);
				else if (env.getDialogActionId() == SETPRO6) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 278068) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 3057);
				else if (env.getDialogActionId() == SETPRO7) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			} else if (targetId == 278066) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 3398);

				else if (env.getDialogActionId() == SET_SUCCEED) {
					if (!giveQuestItem(env, 182205654, 1))
						return true;
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD && targetId == 278047) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
