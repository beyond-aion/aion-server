package quest.idian_depths;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author KEQI
 *
 */
public class _23601UnfurledWingswithEmptyStomachs extends QuestHandler {
	
	private static final int questId = 23601;

	public _23601UnfurledWingswithEmptyStomachs()
	{
		super(questId);
	}
	
	@Override
	public void register() {
		qe.registerQuestNpc(801545).addOnQuestStart(questId);
		qe.registerQuestNpc(801545).addOnTalkEvent(questId);
		qe.registerQuestNpc(801548).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat())
		{
			if(targetId == 801545)
			{
				switch (dialog)
				{
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_SIMPLE:
						giveQuestItem(env, 182213497, 1);
						return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		if(qs.getStatus() == QuestStatus.START)
		{
			if(targetId == 801548)
			{
				switch (dialog)
				{
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 0, 0, true);
						return sendQuestDialog(env, 5);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 801548)
				return sendQuestEndDialog(env);
		}

		return false;
	}

}