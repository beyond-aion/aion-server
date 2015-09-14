package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author Cheatkiller
 *
 */
public class _41540ItAllInTheAether extends QuestHandler {

	private final static int questId = 41540;

	public _41540ItAllInTheAether() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182212539, questId);
		qe.registerQuestNpc(205954).addOnQuestStart(questId);
		qe.registerQuestNpc(205954).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 205954) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else if(dialog == DialogAction.QUEST_ACCEPT_SIMPLE) {
					giveQuestItem(env, 182212539, 1);
					return sendQuestStartDialog(env);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (dialog == DialogAction.QUEST_SELECT) {
				return sendQuestDialog(env, 2375);
			}
			else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
				return defaultCloseDialog(env, 1, 1, true, true);
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205954) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			  removeQuestItem(env, 182212539, 1);
			  changeQuestStep(env, 0, 1, false);
				return HandlerResult.SUCCESS;
		}
		return HandlerResult.FAILED;
	}
}