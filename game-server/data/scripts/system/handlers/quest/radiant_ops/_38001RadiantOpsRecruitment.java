package quest.radiant_ops;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author vlog
 */
public class _38001RadiantOpsRecruitment extends AbstractQuestHandler {

	public _38001RadiantOpsRecruitment() {
		super(38001);
	}

	@Override
	public void register() {
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(799828).addOnTalkEvent(questId);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (player.getLevel() >= 40 && (qs == null || qs.isStartable()))
			QuestService.startQuest(new QuestEnv(null, player, questId));
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 799828) { // Pompo
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 10002);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					changeQuestStep(env, 0, 0, true);
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799828) { // Pompo
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
