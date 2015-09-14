package quest.carving_fortune;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.SystemMessageId;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * Talk with Munin (790001). Find Fissure of Destiny (700551) that connects to Ataxiar (320140000) and talk with Hagen (205020) (spawn). Proceed to
 * Ataxiar aand annihilate the Guardian Legionarys (50): Legionary (798342, 798343, 798344, 798345), Vanquish Brigade General Hellion (1), Talk with
 * Lephar (205118) (spawn). Report the result to Vidar (204052).
 * 
 * @author Bobobear
 */
public class _2099ToFaceTheFuture extends QuestHandler {

	private final static int questId = 2099;
	private final static int[] npcs = { 203550, 205020, 205118, 700551, 204052 };
	private final static int[] mobs = { 798342, 798343, 798344, 798345, 798346 };

	public _2099ToFaceTheFuture() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerOnDie(questId);
		qe.registerOnEnterWorld(questId);
		for (int npc_id : npcs) {
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		}
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203550: { // Munin
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
						}
						case SETPRO1: {
							if ((!giveQuestItem(env, 182207093, 1)) || (!giveQuestItem(env, 182207094, 1)))
								return false;
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				}
				case 700551: { // Fissure of Destiny
					if (env.getDialog() == DialogAction.USE_OBJECT && var == 1) {
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(320140000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						TeleportService2.teleportTo(player, 320140000, newInstance.getInstanceId(), 52, 174, 229);
						return true;
					}
					break;
				}
				case 205020: { // Hagen
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO2: {
							if (var == 1) {
								player.setState(CreatureState.FLIGHT_TELEPORT);
								player.unsetState(CreatureState.ACTIVE);
								player.setFlightTeleportId(1001);
								PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 1001, 0));
								ThreadPoolManager.getInstance().schedule(new Runnable() {

									@Override
									public void run() {
										changeQuestStep(env, 1, 2, false);
									}
								}, 43000);
								return true;
							}
						}
					}
					break;
				}
				case 205118: { // Lephar
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							if (var == 53) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO2:
						case SETPRO3:
							return defaultCloseDialog(env, 53, 53, true, false); // reward
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204052) { // Vidar
				removeQuestItem(env, 182207093, 1);
				removeQuestItem(env, 182207094, 1);
				return sendQuestEndDialog(env);
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
			if (var >= 2 && var < 52) {
				int[] npcIds = { 798342, 798343, 798344, 798345 };
				if (var == 51)
					QuestService.addNewSpawn(320140000, player.getInstanceId(), 798346, 240f, 257f, 208.53946f, (byte) 68);
				return defaultOnKillEvent(env, npcIds, 2, 52); // 2 - 52
			} else if (var == 52) {
				return defaultOnKillEvent(env, 798346, 52, 53); // 53
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var > 1) {
				changeQuestStep(env, var, 1, false); // 1
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId)
					.getName()));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (player.getWorldId() != 320140000) {
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (var > 1) {
					changeQuestStep(env, var, 1, false); // 1
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId)
						.getName()));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2098);
	}
}
