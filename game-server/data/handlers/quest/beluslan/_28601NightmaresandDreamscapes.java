package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */
public class _28601NightmaresandDreamscapes extends AbstractQuestHandler {

	public _28601NightmaresandDreamscapes() {
		super(28601);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204702, 205234 };
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		qe.registerQuestNpc(204702).addOnQuestStart(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 204702) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 182213006, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205234) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 0, 0, true, true, 0, 0, 182213006, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205234) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
