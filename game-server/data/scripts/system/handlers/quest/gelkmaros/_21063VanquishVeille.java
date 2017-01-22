package quest.gelkmaros;

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
 * @author Cheatkiller
 */
public class _21063VanquishVeille extends QuestHandler {

	private final static int questId = 21063;

	public _21063VanquishVeille() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182207852, questId);
		qe.registerQuestNpc(799354).addOnTalkEvent(questId);
		qe.registerQuestNpc(281433).addOnKillEvent(questId);
		qe.registerQuestNpc(799225).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 0) {
				if (dialog == DialogAction.QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799354) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1011);
					else if (qs.getQuestVarById(0) == 2)
						return sendQuestDialog(env, 1693);
				} else if (dialog == DialogAction.SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				} else if (dialog == DialogAction.SET_SUCCEED) {
					giveQuestItem(env, 182207853, 1);
					return defaultCloseDialog(env, 2, 3, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799225) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				removeQuestItem(env, 182207852, 1);
				removeQuestItem(env, 182207853, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 281433, 1, 2);
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}
}
