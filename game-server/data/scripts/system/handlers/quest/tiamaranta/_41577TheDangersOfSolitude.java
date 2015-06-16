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
public class _41577TheDangersOfSolitude extends QuestHandler {

	private final static int questId = 41577;

	public _41577TheDangersOfSolitude() {
		super(questId);
	}

	public void register() {
		qe.registerQuestItem(182213173, questId);
		qe.registerQuestNpc(205962).addOnQuestStart(questId);
		qe.registerQuestNpc(205962).addOnTalkEvent(questId);
		qe.registerQuestNpc(800176).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205962) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else if(dialog == DialogAction.QUEST_ACCEPT_SIMPLE) {
					giveQuestItem(env, 182213173, 1);
					return sendQuestStartDialog(env);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 800176) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				}
				else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 1, 1, true, true);
				}
			}
		}	
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800176) {
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
			  return HandlerResult.fromBoolean(useQuestItem(env, item, 0, 1, false)); 
		}
		return HandlerResult.FAILED;
	}
}

