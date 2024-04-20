package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Akiro, Majka
 */
public class _2231SiblingRivalry extends AbstractQuestHandler {

	public _2231SiblingRivalry() {
		super(2231);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203620).addOnQuestStart(questId);
		qe.registerQuestNpc(203620).addOnTalkEvent(questId);
		qe.registerQuestNpc(203609).addOnTalkEvent(questId);
		qe.registerQuestNpc(203612).addOnTalkEvent(questId);
		qe.registerQuestNpc(203610).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc) {
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		}
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 203620) { // Lamir
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (targetId == 203609) { // Karl
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (env.getDialogActionId() == SETPRO1) {
					return defaultCloseDialog(env, 0, 1); // 1
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (targetId == 203612) { // Gunmarson
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				} else if (env.getDialogActionId() == SETPRO2) {
					return defaultCloseDialog(env, 1, 2); // 2
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (targetId == 203610) { // Kaibech
			if (qs != null) {
				if (env.getDialogActionId() == QUEST_SELECT && qs.getStatus() == QuestStatus.START) {
					return sendQuestDialog(env, 2375);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD && qs.getStatus() != QuestStatus.COMPLETE) {
					qs.setQuestVar(2);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
