package quest.field_wardens;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author vlog
 */
public class _48000SummonsFromTheWardens extends QuestHandler {

	public _48000SummonsFromTheWardens() {
		super(48000);
	}

	@Override
	public void register() {
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(799845).addOnTalkEvent(questId);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (player.getLevel() >= 30 && (qs == null || qs.isStartable()))
			QuestService.startQuest(new QuestEnv(null, player, questId, 0));
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 799845) { // Deryk
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 10002);
				} else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					changeQuestStep(env, 0, 0, true);
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799845) { // Deryk
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
