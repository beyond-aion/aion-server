package quest.eltnen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.World;

/**
 * @author Xitanium
 * @reworked vlog, Pad
 */
public class _1041ADangerousArtifact extends QuestHandler {

	private final static int questId = 1041;

	public _1041ADangerousArtifact() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 203901, 204015, 203833, 278500, 204042, 700181 };
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnLogOut(questId);
		qe.registerAddOnReachTargetEvent(questId);
		qe.registerAddOnLostTargetEvent(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (qs == null) {
			return false;
		}

		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 203901: { // Telemachus
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							} else if (var == 3) {
								return sendQuestDialog(env, 1693);
							} else if (var == 6) {
								return sendQuestDialog(env, 2716);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
						case SETPRO3: {
							if (defaultCloseDialog(env, 3, 4)) {// 4
								TeleportService2.teleportToNpc(player, 203833);
								return true;
							}
						}
						case SETPRO6: {
							return defaultCloseDialog(env, 6, 7); // 7
						}
					}
					break;
				}
				case 204015: { // Civil Engineer
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO2: {
							return defaultStartFollowEvent(env, (Npc) env.getVisibleObject(), 2264.636f, 2359.2563f, 278.62735f, 1, 2);
						}
					}
					break;
				}
				case 203833: { // Xenophon
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 4) {
								return sendQuestDialog(env, 2034);
							}
						}
						case SETPRO4: {
							return defaultCloseDialog(env, 4, 5); // 5
						}
					}
					break;
				}
				case 278500: { // Yuditio
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 5) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SETPRO5: {
							defaultCloseDialog(env, 5, 6); // 6
							TeleportService2.teleportTo(player, 210020000, 266.5f, 2791.77f, 272.48f, (byte) 46);
							return true;
						}
					}
					break;
				}
				case 204042: { // Laigas
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 7) {
								return sendQuestDialog(env, 3057);
							} else if (var == 9) {
								return sendQuestDialog(env, 3398);
							}
						}
						case SELECT_ACTION_3059: {
							return playQuestMovie(env, 37);
						}
						case SELECT_ACTION_3399: {
							return playQuestMovie(env, 38);
						}
						case SETPRO7: {
							giveQuestItem(env, 182201011, 1);
							return defaultCloseDialog(env, 7, 8);
						}
						case SETPRO8: {
							changeQuestStep(env, 9, 9, true); // reward
							return closeDialogWindow(env);
						}
					}
					break;
				}
				case 700181: { // Stolen Artifact
					if (dialog == DialogAction.USE_OBJECT) {
						QuestService.questTimerStart(env, 5);
						return true;
					}
					break;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203901) { // Telemachus
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 3739);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 8) {
			Npc artifact = World.getInstance().getWorldMap(210020000).getMainWorldMapInstance().getNpc(700181);
			if (artifact != null && !artifact.getLifeStats().isAlreadyDead())
				artifact.getController().onDie(env.getPlayer());
			return useQuestObject(env, 8, 9, false, 0, 0, 0, 182201011, 1); // 9
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				changeQuestStep(env, 2, 1, false);
			}
		}
		return false;
	}

	@Override
	public boolean onNpcReachTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 2, 3, false); // 3
	}

	@Override
	public boolean onNpcLostTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 2, 1, false); // 1
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1300, true);
	}
}
