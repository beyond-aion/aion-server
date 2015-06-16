package quest.esoterrace;

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
public class _18400TheVanishings extends QuestHandler
{
	private final static int	questId	= 18400;

	public _18400TheVanishings()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerQuestNpc(799552).addOnQuestStart(questId);
		qe.registerQuestNpc(799552).addOnTalkEvent(questId);
		qe.registerQuestNpc(799584).addOnTalkEvent(questId);
		qe.registerQuestNpc(730014).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if(targetId == 799552)
		{
			if(qs == null || qs.getStatus() == QuestStatus.NONE)
			{
				if(env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}
		else if(targetId == 799584 || targetId == 799585)
		{
			if(qs != null)
			{
				if(env.getDialog() == DialogAction.QUEST_SELECT && qs.getStatus() == QuestStatus.START)
					return sendQuestDialog(env, 2375);
				else if (env.getDialog() == DialogAction.SELECT_QUEST_REWARD)
				{
					qs.setQuestVar(1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				}
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
