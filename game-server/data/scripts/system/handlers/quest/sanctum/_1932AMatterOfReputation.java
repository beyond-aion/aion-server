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
public class _1932AMatterOfReputation extends AbstractQuestHandler {

	private final static int[] npcs = { 203893, 203946 };

	public _1932AMatterOfReputation() {
		super(1932);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203893).addOnQuestStart(questId);
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (sendQuestNoneDialog(env, 203893))
			return true;

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203946:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1352);
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
					}
					break;
				case 203893:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 2375);
							return false;
						case CHECK_USER_HAS_QUEST_ITEM:
							return checkQuestItems(env, 1, 2, true, 5, 2716);
					}
					break;
			}
		}
		return sendQuestRewardDialog(env, 203893, 0);
	}
}
