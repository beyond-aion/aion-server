package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Artur
 * @Modified Majka
 */
public class _14050OrdersFromHeironFortress extends QuestHandler {

	private final static int questId = 14050;

	public _14050OrdersFromHeironFortress() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204500).addOnTalkEvent(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (targetId != 204500)
			return false;
		
		if (qs.getStatus() == QuestStatus.START) {
			if (dialog == DialogAction.QUEST_SELECT) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return sendQuestDialog(env, 10002);
			} else if (dialog== DialogAction.SELECT_QUEST_REWARD) {
				return sendQuestDialog(env, 5);
			}
			return false;
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
	
	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		int[] ids = { 14051, 14052, 14053, 14054 };
		for (int id : ids) {
			QuestEngine.getInstance().onEnterZoneMissionEnd( new QuestEnv(env.getVisibleObject(), env.getPlayer(), id, env.getDialogId()));
		}
		return true;
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 0, true);
	}
}
