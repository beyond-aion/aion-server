package quest.beluslan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 * @Modified Majka
 */
public class _24050NeritasOrders extends QuestHandler {

	private final static int questId = 24050;

	public _24050NeritasOrders() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204702).addOnTalkEvent(questId);
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

		if (targetId != 204702)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			if (dialog == DialogAction.QUEST_SELECT)
				return sendQuestDialog(env, 10002);
			else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return sendQuestDialog(env, 5);
			}
			return false;
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 0, true);
	}
	
	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		int[] ids = { 24051, 24052, 24053, 24054 };
		for (int id : ids)
			QuestEngine.getInstance().onEnterZoneMissionEnd(new QuestEnv(env.getVisibleObject(), env.getPlayer(), id, env.getDialogId()));
		return true;
	}
}
