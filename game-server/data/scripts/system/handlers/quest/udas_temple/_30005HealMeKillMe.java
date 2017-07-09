package quest.udas_temple;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _30005HealMeKillMe extends AbstractQuestHandler {

	private int[] mobs = { 215857, 215814, 215858, 215815 };

	public _30005HealMeKillMe() {
		super(30005);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799029).addOnQuestStart(questId);
		qe.registerQuestNpc(799029).addOnTalkEvent(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (env.getTargetId() == 799029) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (env.getTargetId() == 799029) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var >= 0 && var < 24) {
				return defaultOnKillEvent(env, mobs, 0, 24);
			} else if (var == 24) {
				return defaultOnKillEvent(env, mobs, 24, true);
			}
		}
		return false;
	}
}
