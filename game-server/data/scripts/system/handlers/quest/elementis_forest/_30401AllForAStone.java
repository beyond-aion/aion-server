package quest.elementis_forest;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author zhkchi
 *
 */
public class _30401AllForAStone  extends QuestHandler {

	private final static int questId = 30401;

	public _30401AllForAStone() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799535).addOnQuestStart(questId);
		qe.registerQuestNpc(799535).addOnTalkEvent(questId);
		qe.registerQuestNpc(799582).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(getQuestId());
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 799535) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1011);
					}
					case SELECT_ACTION_1012: {
						return sendQuestDialog(env, 1012);
					}
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 799535) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 2375);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						return checkQuestItems(env, var, var, true, 5, 2716);
					}
					case CHECK_USER_HAS_QUEST_ITEM_SIMPLE: {
						return checkQuestItemsSimple(env, var, var, true, 5, 0, 0);
					}
					case FINISH_DIALOG: {
						return sendQuestSelectionDialog(env);
					}
				}
			}else if(targetId == 799582){
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1352);
					}
					case SELECT_ACTION_1353: {
						return sendQuestDialog(env, 1353);
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1); // 1
					}
					case FINISH_DIALOG: {
						return sendQuestSelectionDialog(env);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799535) {
				return sendQuestEndDialog(env);
			}
		}
		
		
		return false;
	}
	
}
