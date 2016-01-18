package quest.verteron;

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
public class _14010TerrainOfTheVerteronFortress extends QuestHandler {

	private final static int questId = 14010;

	public _14010TerrainOfTheVerteronFortress() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203098).addOnTalkEvent(questId);
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
		
		if (targetId != 203098)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			if (env.getDialog() == DialogAction.QUEST_SELECT) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return sendQuestDialog(env, 1011);
			}
			else
				return sendQuestStartDialog(env);
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
	
	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		int[] ids = { 14011, 14012, 14013, 14014, 14015, 14016 };
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
