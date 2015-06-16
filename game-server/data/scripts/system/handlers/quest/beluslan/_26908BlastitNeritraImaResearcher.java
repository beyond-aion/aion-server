package quest.beluslan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author Ritsu
 *
 */
public class _26908BlastitNeritraImaResearcher extends QuestHandler {

	private static final int questId = 26908;
	private static int[] mobs = {231570, 231571};

	public _26908BlastitNeritraImaResearcher()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerQuestNpc(204702).addOnQuestStart(questId);
		qe.registerQuestNpc(204817).addOnTalkEvent(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
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
			if(targetId == 204702)
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

		if(qs.getStatus() == QuestStatus.START)
		{
			int var = qs.getQuestVarById(0);
			if(targetId == 204817)
			{
				switch (dialog)
				{
					case QUEST_SELECT:
						if(var == 0)
							return sendQuestDialog(env, 1352);
						if(var == 1)
							return sendQuestDialog(env, 2375);
					case SETPRO1:
						changeQuestStep(env, 1, 1, true);
						return closeDialogWindow(env); 
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 1, 1, true);
						return sendQuestDialog(env, 5);
						
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 204817)
				return sendQuestEndDialog(env);
		}

		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return (defaultOnKillEvent(env, mobs, 0, 1));
	}

}
