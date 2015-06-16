package quest.pernon;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * 
 * @author Ritsu
 */
public class _28808OpenSaysMe extends QuestHandler 
{

	private static final int questId = 28808;

	public _28808OpenSaysMe() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830392).addOnQuestStart(questId);
		qe.registerQuestNpc(830392).addOnTalkEvent(questId);
		qe.registerQuestNpc(730534).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if(qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 830392) {
				if (dialog == DialogAction.QUEST_SELECT) 
					return sendQuestDialog(env, 1011); 
				if (dialog == DialogAction.QUEST_ACCEPT_SIMPLE || dialog == DialogAction.QUEST_ACCEPT_1) {
					if (giveQuestItem(env, 182213216, 1))
						return sendQuestStartDialog(env);
					else
						return true;
				}
				else 
					return sendQuestStartDialog(env);
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 730534: {
					switch (dialog)	{
						case USE_OBJECT: {
							if (var == 0) 
								return sendQuestDialog(env, 2375);
						}
						case SELECT_QUEST_REWARD:	{
							changeQuestStep(env, 0, 0, true);
							return sendQuestDialog(env, 5);
						}
					}
				}

			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 730534) 
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
