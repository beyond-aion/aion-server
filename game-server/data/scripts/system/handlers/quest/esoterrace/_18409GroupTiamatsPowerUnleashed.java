package quest.esoterrace;

import java.util.Collections;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu
 * 
 */
public class _18409GroupTiamatsPowerUnleashed extends QuestHandler
{
	private final static int	questId	= 18409;

	public _18409GroupTiamatsPowerUnleashed()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerQuestNpc(799553).addOnQuestStart(questId);
		qe.registerQuestNpc(799553).addOnTalkEvent(questId);
		qe.registerQuestNpc(799552).addOnTalkEvent(questId);
		qe.registerQuestNpc(730014).addOnTalkEvent(questId);
		qe.registerQuestNpc(215795).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if(targetId == 799553)
		{
			if(qs == null || qs.getStatus() == QuestStatus.NONE)
			{
				if(env.getDialogId() == DialogAction.QUEST_SELECT.id())
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}
		else if(targetId == 799552)
		{
			if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0)
			{
				if(env.getDialogId() == DialogAction.QUEST_SELECT.id())
					return sendQuestDialog(env, 1011);
				else if(env.getDialogId() == DialogAction.SETPRO1.id())
				{
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility
						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
				else
					return sendQuestStartDialog(env);
			}
			else if(qs != null && qs.getStatus() == QuestStatus.REWARD)
			{
				if(env.getDialogId() == DialogAction.USE_OBJECT.id())
					return sendQuestDialog(env, 10002);
				else if(env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 5);
				else 
					return sendQuestEndDialog(env);
			}
		}
		else if(targetId == 205232)
		{
			if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1)
			{
				if(env.getDialogId() == DialogAction.QUEST_SELECT.id())
					return sendQuestDialog(env, 1352);
				else if(env.getDialogId() == DialogAction.SETPRO2.id())
				{
					if (!ItemService.addQuestItems(player, Collections.singletonList(new QuestItems(182215007, 1))))
						return true;	
					player.getInventory().decreaseByItemId(182215006, 1);
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					PacketSendUtility
						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
				else
					return sendQuestStartDialog(env);
			}
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = env.getTargetId();

		switch(targetId)
			{				
				case 215795:
					if (qs.getQuestVarById(0) == 2)
						{
							ItemService.addQuestItems(player, Collections.singletonList(new QuestItems(182215008, 1)));	
							player.getInventory().decreaseByItemId(182215007, 1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);	
						}				
			}		
		return false;
	}
}
