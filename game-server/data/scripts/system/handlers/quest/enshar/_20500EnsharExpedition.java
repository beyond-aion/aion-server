package quest.enshar;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 * @Description
 * Go to Enshar and talk with Cogelhogan.
 * Talk with Haldor.
 * 
 * Order: Go to Enshar and meet with Cogelhogan.
 */
public class _20500EnsharExpedition extends QuestHandler {

	private final static int questId = 20500;

	public _20500EnsharExpedition() {
		super(questId);
	}

	@Override
	public void register() {
		// Cogelhogan 804718
		// Haldor 804719
		int[] npcs = { 804718, 804719 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
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
		
		switch(targetId) {
			case 804718: // Cogelhogan
				if (qs.getStatus() == QuestStatus.START) { // Step 0: Go to Enshar and talk with Cogelhogan.
					if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 1011);
					}
					
					if (dialog == DialogAction.SET_SUCCEED) {
						qs.setQuestVar(1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
				}
				break;
			case 804719: // Haldor
				if (qs.getStatus() == QuestStatus.REWARD) { // Step 1: Talk with Haldor.
					if (dialog == DialogAction.USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					} 
					return sendQuestEndDialog(env);
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 0, true);
	}
	
	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		int[] ids = { 20501, 20502, 20503, 20504, 20505, 20506, 20507 };
		for (int id : ids)
			QuestEngine.getInstance().onEnterZoneMissionEnd(new QuestEnv(env.getVisibleObject(), env.getPlayer(), id, env.getDialogId()));
		return true;
	}
}
