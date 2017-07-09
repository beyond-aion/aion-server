package quest.haramel;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _18510MurderMyShugo extends AbstractQuestHandler {

	public _18510MurderMyShugo() {
		super(18510);
	}

	@Override
	public void register() {
		int[] npcs = { 203166, 700953 };
		qe.registerQuestNpc(203166).addOnQuestStart(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(700950).addOnKillEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 700950, 0, 3); // Aether Carts: 1, 2, 3
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 203166) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182212009, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 700953:
					if (dialogActionId == USE_OBJECT) {
						if (var >= 3 && var < 5) {
							return useQuestObject(env, var, var + 1, false, true); // 4,5
						} else if (var == 5) {
							return useQuestObject(env, 5, 5, true, true); // reward
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203166) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
}
