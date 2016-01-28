package quest.ishalgen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author MrPoke
 * @Modified Majka
 */
public class _2100OrderoftheCaptain extends QuestHandler {

	private final static int questId = 2100;

	public _2100OrderoftheCaptain() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203516).addOnTalkEvent(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		if (targetId != 203516)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			if (dialog == DialogAction.QUEST_SELECT) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return sendQuestDialog(env, 1011);
			}
			else
				return sendQuestStartDialog(env);
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}
	
	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		int[] ids = { 2001, 2002, 2003, 2004, 2005, 2006, 2007 };
		for (int id : ids)
			QuestEngine.getInstance().onEnterZoneMissionEnd(new QuestEnv(env.getVisibleObject(), env.getPlayer(), id, env.getDialogId()));
		return true;
	}
}
