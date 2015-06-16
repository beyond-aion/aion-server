package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author vlog
 */
public class _20051EmpiricalTruth extends QuestHandler {

	private static final int questId = 20051;

	public _20051EmpiricalTruth() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 205581, 205617, 205765, 205988 };
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnEnterWorld(questId);
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
		return defaultOnLvlUpEvent(env, 20050, true);
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
				case 205581: { // Kutos
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
				case 205617: { // Aimah
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
					}
					break;
				}
				case 205765: { // Philmoni
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							else if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							return checkQuestItems(env, 3, 4, false, 10000, 10001); // 4
						}
						case SETPRO3: {
							return defaultCloseDialog(env, 2, 3); // 3
						}
						case FINISH_DIALOG: {
							return closeDialogWindow(env);
						}
					}
					break;
				}
				case 730465: { // Mysterious Orb
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 4) {
								WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300390000);
								InstanceService.registerPlayerWithInstance(newInstance, player); 
								TeleportService2.teleportTo(player, 300390000, newInstance.getInstanceId(), 273.96478f, 217.55084f, 207.5269f, (byte) 60,
									TeleportAnimation.BEAM_ANIMATION);
								return true;
							}
						}
					}
					break;
				}
				case 205988: { // Ispharel
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 4, 4, true, false); // reward
						}
					}
					break;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205617) { // Aimah
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

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (player.getWorldId() == 300390000) {
				if (var == 4) {
					return playQuestMovie(env, 703);
				}
			}
		}
		return false;
	}
}
