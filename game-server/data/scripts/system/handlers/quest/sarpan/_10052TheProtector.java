package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author vlog
 */
public class _10052TheProtector extends QuestHandler {

	private static final int questId = 10052;

	public _10052TheProtector() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 205987, 205788, 730474, 205988, 205535, 730465 };
		for (int npcId : npcIds) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(218100).addOnKillEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("SARPAN_CAPITOL_600020000"), questId);
		qe.registerOnEnterZone(ZoneName.get("DEBARIM_PETRALITH_STUDIO_600020000"), questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 10051, true);
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
				case 205987: { // Garnon
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
						case SETPRO5: {
							return defaultCloseDialog(env, 4, 5); // 5
						}
					}
					break;
				}
				case 205788: { // Nytia
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
				case 730474: { // Zayedan's Record
					switch (dialog) {
						case USE_OBJECT: {
							if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
						}
						case SETPRO7: {
							changeQuestStep(env, 6, 7, false); // 7
							return closeDialogWindow(env);
						}
					}
					break;
				}
				case 730465: { // Mysterious Orb
					switch (dialog) {
						case USE_OBJECT: {
							if (var == 7) {
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 1011));
								return true;
							}
						}
						case SETPRO1: {
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300390000);
							InstanceService.registerPlayerWithInstance(newInstance, player);
							TeleportService2.teleportTo(player, 300390000, newInstance.getInstanceId(), 273.96478f, 217.55084f, 207.5269f, (byte) 60,
								TeleportAnimation.FADE_OUT_BEAM);
						}
					}
					break;
				}
				case 205988: { // Ispharel
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 7) {
								return sendQuestDialog(env, 3398);
							}
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 7, 7, true, false); // reward
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205535) { // Killios
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (zoneName.equals(ZoneName.get("SARPAN_CAPITOL_600020000"))) {
				if (var == 0) {
					changeQuestStep(env, 0, 1, false); // 1
					playQuestMovie(env, 703);
					return true;
				}
			} else if (zoneName.equals(ZoneName.get("DEBARIM_PETRALITH_STUDIO_600020000"))) {
				if (var == 5) {
					changeQuestStep(env, 5, 6, false); // 6
					playQuestMovie(env, 705);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 218100, 3, 4); // 4
	}
}
