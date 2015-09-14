package quest.hero;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _13524CompleteTheIdgelResearchCenter extends QuestHandler {

	public static final int questId = 13524;

	public _13524CompleteTheIdgelResearchCenter() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterWorld(questId);
		qe.registerQuestNpc(800915).addOnQuestStart(questId);
		qe.registerQuestNpc(800915).addOnTalkEvent(questId);
		qe.registerQuestNpc(800915).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		QuestState qs1 = player.getQuestStateList().getQuestState(18010);
		QuestState qs2 = player.getQuestStateList().getQuestState(18011);
		QuestState qs3 = player.getQuestStateList().getQuestState(18012);
		QuestState qs4 = player.getQuestStateList().getQuestState(18013);
		QuestState qs5 = player.getQuestStateList().getQuestState(18014);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (targetId == 800918) {
			switch (dialog) {
				case QUEST_SELECT: {
					if (qs1 == null || qs2 == null || qs3 == null || qs4 == null || qs5 == null) {
						return closeDialogWindow(env);
					}

					if (qs1.getStatus() == QuestStatus.COMPLETE && qs2.getStatus() == QuestStatus.COMPLETE && qs3.getStatus() == QuestStatus.COMPLETE
						&& qs4.getStatus() == QuestStatus.COMPLETE && qs5.getStatus() == QuestStatus.COMPLETE) {
						QuestService.startQuest(env);
						return sendQuestDialog(env, 2375);
					} else
						return closeDialogWindow(env);
				}

			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 800915: {
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2375);
						}
						case SELECT_QUEST_REWARD: {
							changeQuestStep(env, 0, 0, true);
							return sendQuestEndDialog(env);
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800915) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		QuestState qs1 = player.getQuestStateList().getQuestState(18010);
		QuestState qs2 = player.getQuestStateList().getQuestState(18011);
		QuestState qs3 = player.getQuestStateList().getQuestState(18012);
		QuestState qs4 = player.getQuestStateList().getQuestState(18013);
		QuestState qs5 = player.getQuestStateList().getQuestState(18014);

		if (qs1 == null || qs2 == null || qs3 == null || qs4 == null || qs5 == null) {
			return false;
		}

		if (qs1.getStatus() == QuestStatus.COMPLETE && qs2.getStatus() == QuestStatus.COMPLETE && qs3.getStatus() == QuestStatus.COMPLETE
			&& qs4.getStatus() == QuestStatus.COMPLETE && qs5.getStatus() == QuestStatus.COMPLETE) {
			if (qs == null || qs.getStatus() == QuestStatus.NONE) {
				QuestService.startQuest(env);
				return true;
			}
		}
		return false;
	}
}
