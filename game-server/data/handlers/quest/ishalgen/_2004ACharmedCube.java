package quest.ishalgen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Mr. Poke, vlog, Majka
 */
public class _2004ACharmedCube extends AbstractQuestHandler {

	private int[] mobs = { 210402, 210403 };

	public _2004ACharmedCube() {
		super(2004);
	}

	@Override
	public void register() {
		int[] npcs = { 203539, 700047, 203550 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203539: // Derot
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
						case SETPRO2:
							giveQuestItem(env, 182203005, 1);
							return sendQuestSelectionDialog(env);
						case CHECK_USER_HAS_QUEST_ITEM:
							return checkQuestItems(env, 1, 2, false, 1438, 1353); // 2
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
					break;
				case 700047: // Tombstone
					if (var == 1 && env.getVisibleObject().getObjectTemplate().getTemplateId() == 700047 && dialogActionId == USE_OBJECT) {
						spawnForFiveMinutesInFront(211755, env.getVisibleObject(), env.getVisibleObject().getHeading(), 2);
						return true;
					}
					return false;
				case 203550: // Munin
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							} else if (var == 6) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3, 0, 0, 182203005, 1); // 3
						case SETPRO4:
							return defaultCloseDialog(env, 6, 6, true, false); // reward
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203539) { // Derot
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, mobs, 3, 6); // 3 - 6
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 2100);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 2100);
	}
}
