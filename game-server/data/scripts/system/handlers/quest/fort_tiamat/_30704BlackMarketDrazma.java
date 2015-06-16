package quest.fort_tiamat;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author Cheatkiller
 *
 */
public class _30704BlackMarketDrazma extends QuestHandler {

	private final static int questId = 30704;
	private final static int npcs [] = {205842, 205890, 205885, 799436};

	public _30704BlackMarketDrazma() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205842).addOnQuestStart(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205842) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205890) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 0) {
							return sendQuestDialog(env, 1352);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}
				}
			}
			else if (targetId == 205885) {
				switch (dialog) {
					case QUEST_SELECT: {
						if(var == 1)
							return sendQuestDialog(env, 1693);
					}
					case SETPRO2: {
						return defaultCloseDialog(env, 1, 2);
						}
					}
				}
			else if (targetId == 799436) {
				switch (dialog) {
					case QUEST_SELECT: {
						if(var == 2)
							return sendQuestDialog(env, 2034);
					}
					case SETPRO3: {
						qs.setQuestVar(3);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return closeDialogWindow(env);
						}
					}
				}
		  }
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205890) { 
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
