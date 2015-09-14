package quest.rentus_base;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author Ritsu
 *
 */
public class _30551OnToRentusOutpost extends QuestHandler
{
	private final static int questId=30551;
	public _30551OnToRentusOutpost()
	{
		super(questId);
	}
	@Override
	public void register()
	{
		qe.registerQuestNpc(799544).addOnQuestStart(questId); //Oreitia
		qe.registerQuestNpc(799544).addOnTalkEvent(questId); 
		qe.registerQuestNpc(799666).addOnTalkEvent(questId); //ariana
		qe.registerOnEnterZone(ZoneName.get("RENTUS_BASE_300280000"), questId);
	}
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		Player player=env.getPlayer();
		QuestState qs=player.getQuestStateList().getQuestState(questId);
		int targetId=env.getTargetId();
		DialogAction dialog=env.getDialog();
		if(qs==null || qs.getStatus() ==QuestStatus.NONE || qs.canRepeat())
		{
			switch(targetId)
			{
				case 799544:
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
		else if(qs != null && qs.getStatus() == QuestStatus.START)
		{
			switch(targetId)
			{
				case 799666:
				{
					switch(dialog)
					{
						case SET_SUCCEED:
						{
							return defaultCloseDialog(env, 1, 1, true, false);//reward
						}
					}
				}
			}
		}
		else if(qs != null && qs.getStatus() == QuestStatus.REWARD)
		{
			switch(targetId)
			{
				case 799666:
				{
					return sendQuestEndDialog(env);
				}
			}
		}	
		return false;
	}
	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START)
		{
			int var = qs.getQuestVarById(0);
			if (zoneName.equals(ZoneName.get("RENTUS_BASE_300280000"))) 
			{
				if (var == 0)
				{
					changeQuestStep(env, 0, 1, true); // 1
					return true;
				}
			}
		}
		return false;
	}
}
