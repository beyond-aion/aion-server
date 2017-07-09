package quest.esoterrace;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _28409GroupMaketheBladeComplete extends AbstractQuestHandler {

	public _28409GroupMaketheBladeComplete() {
		super(28409);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799558).addOnQuestStart(questId);
		qe.registerQuestNpc(799558).addOnTalkEvent(questId);
		qe.registerQuestNpc(799557).addOnTalkEvent(questId);
		qe.registerQuestNpc(205237).addOnTalkEvent(questId);
		qe.registerQuestNpc(215795).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (targetId == 799558) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 799557) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == SETPRO1)
					return defaultCloseDialog(env, 0, 1);
				else
					return sendQuestStartDialog(env);
			} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		} else if (targetId == 205237) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO2)
					return defaultCloseDialog(env, 1, 2, 182215019, 1, 182215018, 1);
				else
					return sendQuestStartDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = env.getTargetId();

		switch (targetId) {
			case 215795:
				if (qs.getQuestVarById(0) == 2) {
					giveQuestItem(env, 182215020, 1);
					player.getInventory().decreaseByItemId(182215019, 1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
				}
		}
		return false;
	}
}
