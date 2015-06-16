package quest.danaria;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author Ritsu
 *
 */
public class _23064GoldrinesPersonalRecognition extends QuestHandler {

	private static final int questId = 23064;

	public _23064GoldrinesPersonalRecognition()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerQuestNpc(801139).addOnQuestStart(questId);
		qe.registerQuestNpc(801139).addOnTalkEvent(questId);
		qe.registerQuestItem(182213444, questId);
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
			if(targetId == 801139)
			{
				switch (dialog)
				{
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_SIMPLE:
						giveQuestItem(env, 182213444, 1);
						return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		if(qs.getStatus() == QuestStatus.START)
		{
			int var = qs.getQuestVarById(0);
			if(targetId == 801139)
			{
				switch (dialog)
				{
					case QUEST_SELECT:
						if(var == 1)
							return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 1, 1, true);
						return sendQuestDialog(env, 5);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 801139)
				return sendQuestEndDialog(env);
		}

		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			changeQuestStep(env, 0, 1, false);
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}

}
