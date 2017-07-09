package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _2925AHeartfeltConfession extends AbstractQuestHandler {

	public _2925AHeartfeltConfession() {
		super(2925);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204261).addOnQuestStart(questId);
		qe.registerQuestNpc(204261).addOnTalkEvent(questId);
		qe.registerQuestNpc(204235).addOnTalkEvent(questId);
		qe.registerQuestNpc(204127).addOnTalkEvent(questId);
		qe.registerQuestNpc(204193).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204261) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204235) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						if (!player.getEquipment().getEquippedItemsByItemId(110100288).isEmpty())
							return sendQuestDialog(env, 1011);
						else
							return sendQuestDialog(env, 1097);
					} else if (qs.getQuestVarById(0) == 4) {
						return sendQuestDialog(env, 2375);
					}
				} else if (dialogActionId == SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				} else if (dialogActionId == SETPRO5) {
					return defaultCloseDialog(env, 4, 5);
				}
			} else if (targetId == 204261) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1352);
					else if (qs.getQuestVarById(0) == 5)
						return sendQuestDialog(env, 2716);
				} else if (dialogActionId == SETPRO2) {
					removeQuestItem(env, 110100288, 1);
					return defaultCloseDialog(env, 1, 2);
				} else if (dialogActionId == SELECT6_1) {
					changeQuestStep(env, 5, 5, true);
					return sendQuestDialog(env, 10002);
				}
			} else if (targetId == 204127) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 2)
						return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SETPRO3) {
					return defaultCloseDialog(env, 2, 3);
				}
			} else if (targetId == 204193) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 3)
						return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SETPRO4) {
					return defaultCloseDialog(env, 3, 4);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204261) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
