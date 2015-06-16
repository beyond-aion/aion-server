package quest.elementis_forest;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * 
 * @author Ritsu
 */
public class _30457SpiritChase extends QuestHandler 
{

	private static final int questId = 30457;

	public _30457SpiritChase()
	{
		super(questId);
	}

	@Override
	public void register() 
	{
		qe.registerQuestNpc(799551).addOnQuestStart(questId);
		qe.registerQuestNpc(799551).addOnTalkEvent(questId);
		qe.registerQuestNpc(205575).addOnTalkEvent(questId);
		qe.registerQuestItem(182213028, questId);
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) 
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		final Npc npc = (Npc) player.getTarget();	
		if (((Npc) player.getTarget()).getNpcId() != 217262)
			return HandlerResult.UNKNOWN;
		if (!MathUtil.isIn3dRange(player, npc, 12.5f))
			return HandlerResult.UNKNOWN;
		if (id != 182213028)
			return HandlerResult.UNKNOWN;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return HandlerResult.FAILED;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0,
			0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run()
			{
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0,
					1, 0), true);

				npc.getController().scheduleRespawn();
				npc.getController().onDelete();
				player.getInventory().decreaseByObjectId(itemObjId, 1);
				giveQuestItem(env, 182213029, 1);
			}
		}, 3000);
		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if(qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat())
		{
			if (targetId == 799551) 
			{
				switch (dialog)
				{
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case QUEST_ACCEPT_SIMPLE:
						if (giveQuestItem(env, 182213014, 1))
							return sendQuestStartDialog(env);
						else
							return true;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START)
		{
			int var = qs.getQuestVarById(0);
			switch (targetId)
			{
				case 799551: 
				{
					switch (dialog)
					{
						case QUEST_SELECT: 
						{
							if (var == 0) 
								return sendQuestDialog(env, 1011);
						}
						case CHECK_USER_HAS_QUEST_ITEM_SIMPLE: 
						{
							if (QuestService.collectItemCheck(env, true)) 
							{
								changeQuestStep(env, 0, 0, true);	
								return sendQuestDialog(env, 10002);
							}
							else
								return closeDialogWindow(env);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) 
		{
			if (targetId == 799551) 
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
