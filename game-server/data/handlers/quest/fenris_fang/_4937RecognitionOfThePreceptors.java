package quest.fenris_fang;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Nanou, vlog
 */
public class _4937RecognitionOfThePreceptors extends AbstractQuestHandler {

	public _4937RecognitionOfThePreceptors() {
		super(4937);
	}

	@Override
	public void register() {
		int[] npcs = { 204053, 204059, 204058, 204057, 204056, 204075, 801222, 801223 };
		qe.registerQuestNpc(204053).addOnQuestStart(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 204053) { // Kvasir
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182207112, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204059: // Freyr
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 204058: // Sif
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2); // 2
					}
					break;
				case 204057: // Sigyn
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							return false;
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3); // 3
					}
					break;
				case 204056: // Traufnir
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SETPRO4:
							return defaultCloseDialog(env, 3, 4, 182207113, 1, 182207112, 1); // 4
					}
					break;
				case 801222: // Hadubrant
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						case SETPRO5:
							return defaultCloseDialog(env, 4, 5);
					}
					break;
				case 801223: // Brynhilde
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 5) {
								return sendQuestDialog(env, 2716);
							}
							return false;
						case SETPRO6:
							return defaultCloseDialog(env, 5, 6);
					}
					break;
				case 204075: // Balder
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 6 && checkItemExistence(env, 182207113, 1, false)) {
								return sendQuestDialog(env, 3057);
							}
							return false;
						case FINISH_DIALOG:
							return defaultCloseDialog(env, var, var);
						case SET_SUCCEED:
							return checkItemExistence(env, 6, 6, true, 186000084, 1, true, 0, 3143, 0, 0); // reward
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204053) { // Kvasir
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					return sendQuestDialog(env, 5);
				} else {
					if (checkItemExistence(env, 182207113, 1, true)) {
						return sendQuestEndDialog(env);
					} else {
						return sendQuestSelectionDialog(env);
					}
				}
			}
		}
		return false;
	}
}
