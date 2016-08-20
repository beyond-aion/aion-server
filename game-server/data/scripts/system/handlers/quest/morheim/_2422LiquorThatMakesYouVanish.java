package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author MrPoke
 * @modified Nephis, Gigi
 * @reworked vlog
 */
public class _2422LiquorThatMakesYouVanish extends QuestHandler {

	private final static int questId = 2422;

	public _2422LiquorThatMakesYouVanish() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204326).addOnQuestStart(questId);
		qe.registerQuestNpc(204326).addOnTalkEvent(questId);
		qe.registerQuestNpc(204327).addOnTalkEvent(questId);
		qe.registerQuestNpc(204375).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 204326) { // Hapenill
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204327: { // Sveinn
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				}
				case 204375: { // Otis
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						}
						case SETPRO2: {
							qs.setQuestVar(2);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							TeleportService2.teleportTo(player, 210020000, player.getInstanceId(), (float) 535.46, (float) 2555.62, (float) 326.63605, (byte) 112);
							return true;
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204326) { // Hapenill
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
