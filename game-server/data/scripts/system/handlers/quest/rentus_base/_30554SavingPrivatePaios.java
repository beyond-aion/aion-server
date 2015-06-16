package quest.rentus_base;

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
public class _30554SavingPrivatePaios extends QuestHandler
{
	private final static int questId=30554;
	public _30554SavingPrivatePaios()
	{
		super(questId);
	}
	@Override
	public void register()
	{
		qe.registerQuestNpc(205438).addOnQuestStart(questId); //Lition
		qe.registerQuestNpc(205438).addOnTalkEvent(questId); 
		qe.registerQuestNpc(799536).addOnTalkEvent(questId); //Paios
		qe.registerQuestNpc(701098).addOnTalkEvent(questId);

	}
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		Player player=env.getPlayer();
		QuestState qs=player.getQuestStateList().getQuestState(questId);
		DialogAction dialog=env.getDialog();
		int targetId=env.getTargetId();
		if(qs==null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat())
		{
			switch(targetId)
			{
				case 205438:
				{
					switch(dialog)
					{
						case QUEST_SELECT:
							return sendQuestDialog(env,4762);

						default:
							return sendQuestStartDialog(env);
					}
				}
			}
		}
		else if(qs!=null && qs.getStatus() ==QuestStatus.START)
		{

			switch(targetId)
			{
				case 799536:
				{
					switch(dialog)
					{
						case QUEST_SELECT:
							return sendQuestDialog(env,1352);
						case SET_SUCCEED:
						{
							return defaultCloseDialog(env, 1, 2, true, false);//reward

						}
					}
				}
				case 701098:
				{
					switch(dialog)
					{
						case USE_OBJECT:
							return useQuestObject(env, 0, 1, false, true);
					}
				}
				case 205438:
				{
					switch(dialog)
					{
						case SELECT_QUEST_REWARD:
							return defaultCloseDialog(env, 2, 2, true, false);
					}
				}
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			switch(targetId)
			{
				case 205438:
				{
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
