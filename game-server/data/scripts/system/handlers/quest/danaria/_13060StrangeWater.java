package quest.danaria;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Ritsu
 */
public class _13060StrangeWater extends QuestHandler {

	private static final int questId = 13060;
	private static List<Integer> npcs = new ArrayList<Integer>();

	static {
		npcs.add(800936);
		npcs.add(800937);
		npcs.add(800938);
	}

	public _13060StrangeWater() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801095).addOnTalkEvent(questId);
		qe.registerQuestNpc(801096).addOnTalkEvent(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(182213452, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 0) {
				if (env.getDialog() == DialogAction.QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 801096) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
						if (var == 2)
							return sendQuestDialog(env, 2034);
					case SETPRO1:
						changeQuestStep(env, 0, 1, false);
						return closeDialogWindow(env);
					case SETPRO3:
						changeQuestStep(env, 2, 3, false);
						return closeDialogWindow(env);
				}
			} else if (targetId == 801095) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1693);
					case SETPRO2:
						changeQuestStep(env, 1, 2, false);
						return closeDialogWindow(env);
				}
			} else if (npcs.contains(targetId)) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 3)
							return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 3, 3, true);
						return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (npcs.contains(targetId))
				return sendQuestEndDialog(env);
		}

		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}

}
