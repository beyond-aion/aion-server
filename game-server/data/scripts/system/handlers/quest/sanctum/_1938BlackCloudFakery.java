package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _1938BlackCloudFakery extends AbstractQuestHandler {

	private final static int[] npcs = { 203703, 279001, 279008 };

	public _1938BlackCloudFakery() {
		super(1938);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203703).addOnQuestStart(questId);
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (sendQuestNoneDialog(env, 203703))
			return true;

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 279001:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1352);
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
					}
					break;
				case 279008:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1693);
							return false;
						case SETPRO2:
							qs.setQuestVar(2);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
			}
		}
		return sendQuestRewardDialog(env, 203703, 2375);
	}
}
