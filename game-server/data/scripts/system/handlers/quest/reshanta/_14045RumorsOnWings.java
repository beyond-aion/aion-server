package quest.reshanta;

import java.util.List;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Artur
 * @Modified Majka
 */
public class _14045RumorsOnWings extends QuestHandler {

	private final static int questId = 14045;

	public _14045RumorsOnWings() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 278506, 279023, 278643 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 278506: { // Tellus
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						}
						case SELECT_ACTION_1013: {
							playQuestMovie(env, 272);
							break;
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				}
				case 279023: { // Agemonerk
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						}
						case SETPRO2: {
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
							player.setState(CreatureState.FLIGHT_TELEPORT);
							player.unsetState(CreatureState.ACTIVE);
							player.setFlightTeleportId(57001);
							PacketSendUtility.broadcastPacketAndReceive(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 57001, 0));
							return defaultCloseDialog(env, 1, 2); // 2
						}
					}
					break;
				}
				case 278643: { // Raithor
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							} else if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						}
						case SETPRO3: {
							if (var == 2) {
								QuestService.addNewSpawn(400010000, player.getInstanceId(), 214102, 2344.32f, 1789.96f, 2258.88f, (byte) 86, 5);
								QuestService.addNewSpawn(400010000, player.getInstanceId(), 214102, 2344.51f, 1786.01f, 2258.88f, (byte) 52, 5);
								Creature raithor = World.getInstance().getWorldMap(400010000).getWorldMapInstanceById(player.getInstanceId()).getNpc(278643);
								List<Npc> npcs = World.getInstance().getWorldMap(400010000).getWorldMapInstanceById(player.getInstanceId()).getNpcs(214102);
								for (Npc npc : npcs) {
									npc.getAggroList().startHate(raithor);
									raithor.getAggroList().startHate(npc);
								}
								qs.setQuestVarById(0, 3); // 3
								return closeDialogWindow(env);
							}
							return false;
						}
						case SETPRO4: {
							if (var == 3) {
								qs.setQuestVarById(0, 12);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								player.setState(CreatureState.FLIGHT_TELEPORT);
								player.unsetState(CreatureState.ACTIVE);
								player.setFlightTeleportId(58001);
								PacketSendUtility.broadcastPacketAndReceive(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 58001, 0));
								return sendQuestSelectionDialog(env);
							}
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 279023) { // Agemonerk
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
	public void onQuestCompletedEvent(QuestEnv env) {
		int[] quests = { 14040, 14041 };
		defaultOnQuestCompletedEvent(env, quests);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		int[] quests = { 14040, 14041 };
		defaultOnLevelChangedEvent(player, quests);
	}

}
