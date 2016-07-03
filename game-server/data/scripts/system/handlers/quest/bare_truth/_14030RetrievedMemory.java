package quest.bare_truth;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Artur
 * @modified Pad
 */
public class _14030RetrievedMemory extends QuestHandler {

	private final static int[] npcs = { 790001, 700551, 205119, 700552, 203700 };
	private final static int[] mobs = { 211043, 214578, 215396, 215397, 215398, 215399, 215400 };

	public _14030RetrievedMemory() {
		super(14030);
	}

	@Override
	public void register() {
		qe.registerOnLevelChanged(questId);
		qe.registerOnEnterWorld(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203700: { // Fasimedes
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							break;
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				}
				case 790001: { // Pernos
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							break;
						}
						case SETPRO2: {
							TeleportService2.teleportTo(player, 210060000, 2012.37f, 438.231f, 126.020f, (byte) 7, TeleportAnimation.FADE_OUT_BEAM);
							return defaultCloseDialog(env, 1, 2); // 2
						}
						case SETPRO4: {
							if (!giveQuestItem(env, 182215387, 1))
								return false;
							return defaultCloseDialog(env, 3, 4); // 4
						}
					}
					break;
				}
				case 700551: { // Fissure of Destiny
					if (dialog == DialogAction.USE_OBJECT && var == 4) {
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(310120000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						TeleportService2.teleportTo(player, 310120000, newInstance.getInstanceId(), 52, 174, 229);
						return true;
					}
					break;
				}
				case 205119: { // Hermione
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
							break;
						}
						case SETPRO5: {
							if (var == 4) {
								player.setState(CreatureState.FLIGHT_TELEPORT);
								player.unsetState(CreatureState.ACTIVE);
								player.setFlightTeleportId(1001);
								PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 1001, 0));
								changeQuestStep(env, 4, 5, false);
								return true;
							}
							break;
						}
					}
					break;
				}
				case 700552: { // Artifact of Memory
					if (dialog == DialogAction.USE_OBJECT && var == 56) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						TeleportService2.teleportTo(player, 110010000, 1876.29f, 1511f, 812.675f, (byte) 60, TeleportAnimation.FADE_OUT_BEAM);
						return useQuestObject(env, 56, 56, false, 0, 0, 0, 182215387, 1, 0, false); // 56
					}
					break;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203700) { // Fasimedes
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 3739);
					}
					case SELECT_QUEST_REWARD: {
						return sendQuestDialog(env, 5);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var >= 2 && var < 55) {
				int[] npcIds = { 215396, 215397, 215398, 215399, 211043, 214578 };
				if (var == 2) {
					return defaultOnKillEvent(env, 214578, 2, 3); // 3
				}
				if (var == 54) {
					QuestService.addNewSpawn(310120000, player.getInstanceId(), 215400, 240f, 257f, 208.53946f, (byte) 68);
				}
				return defaultOnKillEvent(env, npcIds, 2, 55); // 2 - 55
			} else if (var == 55) {
				return defaultOnKillEvent(env, 215400, 55, 56); // 56
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		if (env.getPlayer().getWorldId() != 310120000) {
			QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);

			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var0 = qs.getQuestVarById(0);
				if (var0 > 4 && var0 <= 56) {
					qs.setQuestVarById(0, 4);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}
}
