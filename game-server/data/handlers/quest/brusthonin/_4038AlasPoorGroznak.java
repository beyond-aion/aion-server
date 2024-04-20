package quest.brusthonin;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Nephis, vlog
 */
public class _4038AlasPoorGroznak extends AbstractQuestHandler {

	public _4038AlasPoorGroznak() {
		super(4038);
	}

	@Override
	public void register() {
		int[] npcs = { 205150, 730155, 700380, 700381, 700382 };
		qe.registerQuestNpc(205150).addOnQuestStart(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 205150) { // Surt
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 730155: { // Groznak's Skull
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							} else if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
						case CHECK_USER_HAS_QUEST_ITEM:
							return checkQuestItems(env, 1, 2, false, 10000, 10001); // 2
						case SETPRO3:
							return defaultCloseDialog(env, 2, 2, true, false); // reward
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
					break;
				}
				case 700380: // Weathered Skeleton
					if (var == 1) {
						return true; // loot
					}
					break;
				case 700381: // Intact Skeleton
					if (var == 1) {
						return true; // loot
					}
					break;
				case 700382: // Muddy Skeleton
					if (var == 1) {
						return true; // loot
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205150) { // Surt
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
