package quest.silentera_canyon;

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
public class _30158JotunJunket extends QuestHandler
{
	private final static int	questId	= 30158;

	public _30158JotunJunket()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerQuestNpc(799383).addOnQuestStart(questId);
		qe.registerQuestNpc(799383).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("UNKNOWN_LANDS_600010000"), questId);
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName)
	{
		if(zoneName != ZoneName.get("UNKNOWN_LANDS_600010000"))
			return false;
		final Player player = env.getPlayer();
		if (player == null)
			return false;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getQuestVars().getQuestVars() != 0)
			return false;
		if (qs.getStatus() != QuestStatus.START)
			return false;
		env.setQuestId(questId);
		qs.setStatus(QuestStatus.REWARD);
		updateQuestStatus(env);
		return true;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		if(targetId == 799383)
		{
			if(qs == null || qs.getStatus() == QuestStatus.NONE)
			{
				if(dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}

			else if(qs != null && qs.getStatus() == QuestStatus.REWARD)
			{
				if(dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if(dialog == DialogAction.SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else 
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
