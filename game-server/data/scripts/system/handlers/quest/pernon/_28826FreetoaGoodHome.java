package quest.pernon;

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
public class _28826FreetoaGoodHome extends QuestHandler {
	
	private static final int questId = 28826;

	public _28826FreetoaGoodHome() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830663).addOnQuestStart(questId);
		qe.registerQuestNpc(830663).addOnTalkEvent(questId);
		qe.registerQuestNpc(830521).addOnQuestStart(questId);
		qe.registerQuestNpc(830521).addOnTalkEvent(questId);
		qe.registerQuestNpc(830662).addOnQuestStart(questId);
		qe.registerQuestNpc(830662).addOnTalkEvent(questId);
		qe.registerQuestNpc(730525).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 830662 || targetId == 830663 || targetId == 830521) {
				if (dialog == DialogAction.QUEST_SELECT) 
					return sendQuestDialog(env, 1011);
				else 
					return sendQuestStartDialog(env);
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 730525) {
				switch (dialog)	{
					case USE_OBJECT: {
						return sendQuestDialog(env, 2375);
					}
					case SELECT_QUEST_REWARD:	{
						changeQuestStep(env, 0, 0, true);
						return sendQuestDialog(env, 5);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD && targetId == 730525) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
