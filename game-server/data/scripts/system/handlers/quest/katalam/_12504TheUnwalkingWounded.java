package quest.katalam;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * 
 * @author Ritsu
 */
public class _12504TheUnwalkingWounded extends QuestHandler 
{

	private static final int questId = 12504;

	public _12504TheUnwalkingWounded() 
	{
		super(questId);
	}

	@Override
	public void register() 
	{
		qe.registerQuestNpc(801313).addOnQuestStart(questId);
		qe.registerQuestNpc(801313).addOnTalkEvent(questId);
		qe.registerQuestNpc(800526).addOnTalkEvent(questId);

	}

	@Override
	public boolean onDialogEvent(QuestEnv env) 
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if(qs == null || qs.getStatus() == QuestStatus.NONE)
		{
			if (targetId == 801313) 
			{
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 4);
				else if (dialog == DialogAction.OPEN_STIGMA_WINDOW)
				{
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}

				else 
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;
		else if (qs.getStatus() == QuestStatus.START) 
		{
			int var = qs.getQuestVarById(0);
			switch (targetId) 
			{
				case 801313: 
				{
					switch (dialog) 
					{
						case QUEST_SELECT: 
						{
							if (var == 0) 
							{
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO1: 
						{
							return defaultCloseDialog(env, 0, 1);
						}
					}
				}
				break;
				case 800526:
				{
					switch (dialog) 
					{
						case QUEST_SELECT: 
						{
							if (var == 1) 
							{
								return sendQuestDialog(env, 2375);
							}
						}
						case SELECT_QUEST_REWARD: 
						{
							return defaultCloseDialog(env, 1, 1, true, true, 5);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if (targetId == 800526)
			{
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
