package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */
public class _18601NightmareonMyStreets extends QuestHandler {

	private final static int questId = 18601;
	private final static int[] npc_ids = { 204500, 205229 };

	public _18601NightmareonMyStreets() {
		super(questId);
	}

	@Override
	public void register() {
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		qe.registerQuestNpc(204500).addOnQuestStart(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 204500) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 182213002, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205229) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				} else if (env.getDialog() == DialogAction.SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 0, 0, true, true, 0, 0, 182213002, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205229) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
