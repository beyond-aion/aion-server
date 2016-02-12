package quest.theobomos;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Dune11
 * @modified Pad
 */
public class _1091ARequestFromAtropos extends QuestHandler {

	private final static int questId = 1091;

	public _1091ARequestFromAtropos() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798155).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int targetId = env.getTargetId();
		if (targetId != 798155)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			if (env.getDialog() == DialogAction.QUEST_SELECT)
				return sendQuestDialog(env, 10002);
			else if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id()) {
				qs.setStatus(QuestStatus.REWARD);
				qs.setQuestVarById(0, 1);
				updateQuestStatus(env);
				return sendQuestDialog(env, 5);
			}
			return false;
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}

}
