package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author Ritsu
 */
public class _24070ForeignLandFamiliarPlot extends QuestHandler {

	private static final int questId = 24070;

	public _24070ForeignLandFamiliarPlot() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 205617, 205585, 205739, 730468 };
		qe.registerOnLevelUp(questId);
		for (int npcId : npcIds) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 205617: { // Aimah
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				}
				case 205585: { // Rafael
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							if (var == 4)
								return sendQuestDialog(env, 2375);
						}
						case SELECT_ACTION_1353: {
							if (var == 1) {
								sendQuestDialog(env, 1352);
								return playQuestMovie(env, 714);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							return checkQuestItems(env, 4, 4, true, 10000, 10001);
						}
					}
					break;
				}
				case 205739: { // Eyriss
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO3: {
							return defaultCloseDialog(env, 2, 3); // 3
						}
					}
					break;
				}
				case 730468: { // Mephitic Caverns Sealing Gate
					switch (dialog) {
						case USE_OBJECT: {
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
						}
						case SETPRO4: {
							if (var == 3) {
								changeQuestStep(env, 3, 4, false); // 4
								TeleportService2.teleportTo(player, 600020000, 1, 1302, 1825, 500.48282f, (byte) 0,
									TeleportAnimation.BEAM_ANIMATION);
								return closeDialogWindow(env);
							}
						}
					}
					break;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205585) { // Rafael
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
