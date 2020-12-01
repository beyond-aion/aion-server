package quest.eltnen;

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
 * @author Xitanium
 */
public class _1319PrioritesMoney extends AbstractQuestHandler {

	public _1319PrioritesMoney() {
		super(1319);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203908).addOnQuestStart(questId); // Priorite
		qe.registerQuestNpc(203908).addOnTalkEvent(questId); // Priorite
		qe.registerQuestNpc(203923).addOnTalkEvent(questId); // Krato
		qe.registerQuestNpc(203910).addOnTalkEvent(questId); // Hebestis
		qe.registerQuestNpc(203906).addOnTalkEvent(questId); // Benos
		qe.registerQuestNpc(203915).addOnTalkEvent(questId); // Diokles
		qe.registerQuestNpc(203907).addOnTalkEvent(questId); // Tuskeos
		qe.registerQuestNpc(798050).addOnTalkEvent(questId); // Girrinerk
		qe.registerQuestNpc(798049).addOnTalkEvent(questId); // Shaoranyerk
		qe.registerQuestNpc(205240).addOnTalkEvent(questId); // Arnesonerk
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) { // NEED FIX ITEM
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 203908) { // Priorite
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) { // Reward
			if (env.getDialogActionId() == QUEST_SELECT)
				return sendQuestDialog(env, 4080);
			else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
				qs.setQuestVar(8);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return sendQuestEndDialog(env);
			} else
				return sendQuestEndDialog(env);
		} else if (targetId == 203923) { // Krato

			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO1) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}

		} else if (targetId == 203910) { // Hebestis
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO2) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 203906) { // Benos
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2034);
				else if (env.getDialogActionId() == SETPRO3) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 203915) { // Diokles
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 3) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SETPRO4) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 203907) { // Tuskeos
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 4) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2716);
				else if (env.getDialogActionId() == SETPRO5) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 798050) { // Girrinerk
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 5) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 3057);
				else if (env.getDialogActionId() == SETPRO6) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 798049) { // Shaoranranerk
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 6) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 3398);
				else if (env.getDialogActionId() == SETPRO7) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 205240) { // Arnesonerk
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 7) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 3739);
				else if (env.getDialogActionId() == SETPRO8 && qs.getStatus() != QuestStatus.COMPLETE) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		}

		return false;
	}
}
