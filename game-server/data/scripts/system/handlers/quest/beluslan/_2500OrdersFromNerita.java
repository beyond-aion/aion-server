package quest.beluslan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Rhys2002
 * @Modified Majka
 */
public class _2500OrdersFromNerita extends QuestHandler {

	private final static int questId = 2500;

	public _2500OrdersFromNerita() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204702).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (targetId != 204702)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			if (dialog == DialogAction.QUEST_SELECT)
				return sendQuestDialog(env, 10002);
			else if (dialog == DialogAction.SELECTED_QUEST_REWARD3) {
				qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
				return sendQuestDialog(env, 5);
			}
			return false;
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
