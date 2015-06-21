package quest.abyssal_splinter;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Rikka
 */
public class _30265APolearmWalksintoaBar extends QuestHandler {

	private final static int questId = 30265;

	public _30265APolearmWalksintoaBar() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203830).addOnTalkEvent(questId);
		qe.registerQuestNpc(203058).addOnTalkEvent(questId);
		qe.registerQuestNpc(790001).addOnTalkEvent(questId);
		qe.registerQuestItem(182209803, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			return false;
		}
		else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 203830: { // Fuchsia
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				}
				case 203058: { // Asteros
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2, false, false); // 2
						}
					}
					break;
				}
				case 790001: { // Aratus
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SELECT_QUEST_REWARD: {
							changeQuestStep(env, 2, 2, true); // reward
							return sendQuestDialog(env, 5);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 790001) { // Aratus
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			return HandlerResult.fromBoolean(QuestService.startQuest(env));
    }
		return HandlerResult.FAILED;
	}
}
