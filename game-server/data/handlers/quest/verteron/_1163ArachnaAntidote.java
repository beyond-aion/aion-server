package quest.verteron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar
 */
public class _1163ArachnaAntidote extends AbstractQuestHandler {

	public _1163ArachnaAntidote() {
		super(1163);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203096).addOnQuestStart(questId);
		qe.registerQuestNpc(203096).addOnTalkEvent(questId);
		qe.registerQuestNpc(203151).addOnTalkEvent(questId);
		qe.registerQuestNpc(203155).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203096) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env, 182200564, 1);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203151:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1352);
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1, 0, 0, 182200564, 1);
						}
						default: {
							return sendQuestEndDialog(env);
						}
					}
				case 203155:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2375);
						}
						case SELECT_QUEST_REWARD: {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestSelectionDialog(env);
						}
						default: {
							return sendQuestEndDialog(env);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203155) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
