package quest.katalam;

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
public class _12506MilitaryIntelligence extends QuestHandler 
{

	private static final int questId = 12506;

	public _12506MilitaryIntelligence() 
	{
		super(questId);
	}

	@Override
	public void register() 
	{
		qe.registerQuestNpc(801026).addOnQuestStart(questId);
		qe.registerQuestNpc(801026).addOnTalkEvent(questId);
		qe.registerQuestNpc(801760).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if(qs == null || qs.getStatus() == QuestStatus.NONE)
		{
			if(targetId == 801026)
			{
				switch (dialog)
				{
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;
		else if (qs.getStatus() == QuestStatus.START) 
		{
			int var = qs.getQuestVarById(0);
			switch (targetId) 
			{
				case 801760: 
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
				case 801026:
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
						case CHECK_USER_HAS_QUEST_ITEM_SIMPLE: 
						{
							return checkQuestItems(env, 1, 1, true, 5, 0);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if (targetId == 801026)
			{
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
