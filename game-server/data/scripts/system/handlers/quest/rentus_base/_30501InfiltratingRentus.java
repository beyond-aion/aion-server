package quest.rentus_base;

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
public class _30501InfiltratingRentus extends QuestHandler{

	private static final int questId = 30501;

	public _30501InfiltratingRentus() {
		super(questId);
	}
	
	@Override
	public void register() {
		qe.registerQuestNpc(799544).addOnQuestStart(questId);
		qe.registerQuestNpc(799666).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 799544) {
				switch (dialog) {
					case QUEST_SELECT:{
						return sendQuestDialog(env, 4762);
					}
					default:
						return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if(targetId == 799666){
				switch (dialog) {
					case USE_OBJECT:{
						return sendQuestDialog(env, 10002);
					}
					case SELECT_QUEST_REWARD:{
						changeQuestStep(env, 0, 0, true);
						updateQuestStatus(env);
						return sendQuestDialog(env, 5);
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if(targetId == 799666){
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}

