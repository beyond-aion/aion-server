package quest.event_quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.EmotionId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _80020EventSoloriusJoy extends AbstractQuestHandler {

	private final static int[] npcs = { 799769, 799768, 203170, 203140 };

	public _80020EventSoloriusJoy() {
		super(80020);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799769).addOnQuestStart(questId);
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (env.getTargetId() == 799769) {
				if (env.getDialogActionId() == USE_OBJECT || env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestNoneDialog(env, 799769, 182214012, 1);
			}
			return false;
		}

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 799768) {
				if (env.getDialogActionId() == USE_OBJECT || env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO1) {
					defaultCloseDialog(env, 0, 1, 182214013, 2, 182214012, 1);
					return true;
				} else
					return sendQuestStartDialog(env);
			} else if (env.getTargetId() == 203170 && var == 1) {
				if (env.getDialogActionId() == USE_OBJECT || env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SELECT3_1) {
					sendEmotion(env, (Creature) env.getVisibleObject(), EmotionId.NO, true);
					return sendQuestDialog(env, 1694);
				} else if (env.getDialogActionId() == SETPRO2) {
					defaultCloseDialog(env, 1, 2, 0, 0, 182214013, 1);
					return true;
				} else
					return sendQuestStartDialog(env);
			} else if (env.getTargetId() == 203140 && var == 2) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2034);
				else if (env.getDialogActionId() == SELECT4_1) {
					sendEmotion(env, (Creature) env.getVisibleObject(), EmotionId.PANIC, true);
					return sendQuestDialog(env, 2035);
				} else if (env.getDialogActionId() == SETPRO3)
					return defaultCloseDialog(env, 2, 3, true, false, 0, 0, 182214013, 1);
				else
					return sendQuestStartDialog(env);
			}
		}

		return sendQuestRewardDialog(env, 799769, 2375);
	}
}
