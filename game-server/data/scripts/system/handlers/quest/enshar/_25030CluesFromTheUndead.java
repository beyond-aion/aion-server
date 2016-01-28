package quest.enshar;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 */
public class _25030CluesFromTheUndead extends QuestHandler {

	private final static int questId = 25030;

	public _25030CluesFromTheUndead() {
		super(questId);
	}

	@Override
	public void register() {
		// Engrid 804728
		// Egzen 804729
		// Sigmuel 804911
		// Redelf 804913
		qe.registerQuestNpc(804729).addOnQuestStart(questId);
		int[] npcs = { 804728, 804729, 804911, 804913 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 804729) { // Egzen
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			
			switch(targetId) {
				case 804729: // Egzen
					if(var == 0) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM)
							return checkQuestItems(env, var, var+1, false, 10000, 10001, 182215712, 1); // Tarnished Symbol
					}
					if(var == 1) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialog == DialogAction.SETPRO2)
							return defaultCloseDialog(env, var, var+1);
					}
					break;
				case 804913: // Redelf
					if(var == 2) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1693);

						if (dialog == DialogAction.SETPRO3)
							return defaultCloseDialog(env, var, var+1);
					}
					break;
				case 804728: // Engrid
					if(var == 3) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 2034);
						
						if (dialog == DialogAction.SET_SUCCEED) {
							qs.setQuestVar(var+1);
							return defaultCloseDialog(env, var+1, var+1, true, false, 0, 0, 182215712, 1);
						}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch(targetId) {
				case 804911: // Sigmuel
					if (dialog == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 10002);
					
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}