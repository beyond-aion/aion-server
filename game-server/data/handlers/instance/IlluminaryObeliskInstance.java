package instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Estrayl
 */
@InstanceID(301230000)
public class IlluminaryObeliskInstance extends GeneralInstanceHandler {

	private final AtomicBoolean isRaceSet = new AtomicBoolean();
	private final List<Future<?>> tasks = new ArrayList<>();
	public boolean isInstanceDestroyed;

	public IlluminaryObeliskInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		scheduleInstanceStart();
	}

	private void scheduleInstanceStart() {
		ThreadPoolManager.getInstance().schedule(() -> {
			PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_DOOR_OPEN());
			instance.setDoorState(129, true);
			scheduleWipe(3000);
		}, 60000);
	}

	protected void scheduleWipe(int delay) {
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			if (isInstanceDestroyed)
				return;
			switch (delay) {
				case 3000: // 30min
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_GAME_TIMER_01());
					scheduleWipe(300000);
					break;
				case 300000: // 25min
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_GAME_TIMER_02());
					scheduleWipe(300001);
					break;
				case 300001: // 20min
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_GAME_TIMER_03());
					scheduleWipe(300002);
					break;
				case 300002: // 15min
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_GAME_TIMER_04());
					scheduleWipe(300003);
					break;
				case 300003: // 10min
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_GAME_TIMER_05());
					scheduleWipe(300004);
					break;
				case 300004: // 5min
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_GAME_TIMER_06());
					scheduleWipe(240000);
					break;
				case 240000: // 1min
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_GAME_TIMER_07());
					scheduleWipe(60000);
					break;
				case 60000: // wipe
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_GAME_TIMER_08());
					wipe();
					break;
			}
		}, delay));
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (object instanceof Npc npc) {
			int npcId = npc.getNpcId();
			switch (npc.getNpcId()) {
				case 702218:
				case 702219:
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_01());
					break;
				case 702220:
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_01());
					checkGenerators();
					break;
				case 702221:
				case 702222:
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_02());
					break;
				case 702223:
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_FINAL_CHARGE_02());
					checkGenerators();
					break;
				case 702224:
				case 702225:
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_03());
					break;
				case 702226:
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_FINAL_CHARGE_03());
					checkGenerators();
					break;
				case 702227:
				case 702228:
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_04());
					break;
				case 702229:
					PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_FINAL_CHARGE_04());
					checkGenerators();
					break;
			}
			scheduleChargeAttacks(npcId);
		}
	}

	protected void scheduleChargeAttacks(int npcId) {
		switch (npcId) {
			case 702218: // east first wave
				spawn(233720, 255.3635f, 328.5584f, 325.0038f, (byte) 90, 0, "idf5_u3_east_2");
				spawn(233721, 258.5159f, 328.5792f, 325.0038f, (byte) 90, 0, "idf5_u3_east_3");
				spawn(233721, 252.3243f, 328.5881f, 325.0092f, (byte) 90, 0, "idf5_u3_east_4");
				spawn(233722, 255.3635f, 328.5584f, 325.0038f, (byte) 90, 15000, "idf5_u3_east_2");
				spawn(233720, 258.5159f, 328.5792f, 325.0038f, (byte) 90, 15000, "idf5_u3_east_3");
				spawn(233720, 252.3243f, 328.5881f, 325.0092f, (byte) 90, 15000, "idf5_u3_east_4");
				spawn(233723, 255.3635f, 328.5584f, 325.0038f, (byte) 90, 30000, "idf5_u3_east_2");
				spawn(233726, 258.5159f, 328.5792f, 325.0038f, (byte) 90, 30000, "idf5_u3_east_3");
				spawn(233726, 252.3243f, 328.5881f, 325.0092f, (byte) 90, 30000, "idf5_u3_east_4");
				break;
			case 702219: // east second wave
				spawn(233723, 255.3635f, 328.5584f, 325.0038f, (byte) 90, 0, "idf5_u3_east_2");
				spawn(233726, 258.5159f, 328.5792f, 325.0038f, (byte) 90, 0, "idf5_u3_east_3");
				spawn(233726, 252.3243f, 328.5881f, 325.0092f, (byte) 90, 0, "idf5_u3_east_4");
				spawn(233728, 255.3635f, 328.5584f, 325.0038f, (byte) 90, 15000, "idf5_u3_east_2");
				spawn(233721, 258.5159f, 328.5792f, 325.0038f, (byte) 90, 15000, "idf5_u3_east_3");
				spawn(233721, 252.3243f, 328.5881f, 325.0092f, (byte) 90, 15000, "idf5_u3_east_4");
				spawn(233722, 255.3635f, 328.5584f, 325.0038f, (byte) 90, 30000, "idf5_u3_east_2");
				spawn(233720, 258.5159f, 328.5792f, 325.0038f, (byte) 90, 30000, "idf5_u3_east_3");
				spawn(233720, 252.3243f, 328.5881f, 325.0092f, (byte) 90, 30000, "idf5_u3_east_4");
				break;
			case 702220: // east third wave
				spawn(233721, 252.3243f, 328.5881f, 325.0092f, (byte) 90, 0, "idf5_u3_east_1");
				spawn(233726, 255.3635f, 328.5584f, 325.0038f, (byte) 90, 0, "idf5_u3_east_2");
				spawn(233721, 256.6376f, 328.7015f, 325.0038f, (byte) 90, 0, "idf5_u3_east_3");
				spawn(233726, 258.5159f, 328.5792f, 325.0038f, (byte) 90, 0, "idf5_u3_east_4");
				spawn(233736, 253.8757f, 326.5010f, 325.0038f, (byte) 90, 0, "idf5_u3_east_6");
				spawn(233720, 255.3635f, 328.5584f, 325.0038f, (byte) 90, 0, "idf5_u3_east_2");
				spawn(233724, 256.6376f, 328.7015f, 325.0038f, (byte) 90, 0, "idf5_u3_east_3");
				spawn(233720, 258.5159f, 328.5792f, 325.0038f, (byte) 90, 0, "idf5_u3_east_4");
				spawn(233733, 256.9199f, 326.4982f, 325.0038f, (byte) 90, 0, "idf5_u3_east_5");
				break;
			case 702221: // west first wave
				spawn(233720, 253.5314f, 183.5728f, 325.0038f, (byte) 30, 0, "idf5_u3_west_2");
				spawn(233723, 255.2491f, 183.4584f, 325.0038f, (byte) 30, 0, "idf5_u3_west_3");
				spawn(233720, 257.0595f, 183.5797f, 325.0045f, (byte) 30, 0, "idf5_u3_west_4");
				spawn(233721, 253.5314f, 183.5728f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_2");
				spawn(233724, 255.2491f, 183.4584f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_3");
				spawn(233721, 257.0595f, 183.5797f, 325.0045f, (byte) 30, 15000, "idf5_u3_west_4");

				spawn(233722, 253.5314f, 183.5728f, 325.0038f, (byte) 30, 30000, "idf5_u3_west_2");
				spawn(233725, 255.2491f, 183.4584f, 325.0038f, (byte) 30, 30000, "idf5_u3_west_3");
				spawn(233722, 257.0595f, 183.5797f, 325.0045f, (byte) 30, 30000, "idf5_u3_west_4");
				break;
			case 702222: // west second wave
				spawn(233721, 253.5314f, 183.5728f, 325.0038f, (byte) 30, 0, "idf5_u3_west_2");
				spawn(233720, 255.2491f, 183.4584f, 325.0038f, (byte) 30, 0, "idf5_u3_west_3");
				spawn(233721, 257.0595f, 183.5797f, 325.0045f, (byte) 30, 0, "idf5_u3_west_4");
				spawn(233726, 253.5314f, 183.5728f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_2");
				spawn(233727, 255.2491f, 183.4584f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_3");
				spawn(233726, 257.0595f, 183.5797f, 325.0045f, (byte) 30, 15000, "idf5_u3_west_4");

				spawn(233725, 253.5314f, 183.5728f, 325.0038f, (byte) 30, 30000, "idf5_u3_west_2");
				spawn(233732, 255.2491f, 183.4584f, 325.0038f, (byte) 30, 30000, "idf5_u3_west_3");
				spawn(233725, 257.0595f, 183.5797f, 325.0045f, (byte) 30, 30000, "idf5_u3_west_4");
				break;
			case 702223: // west third wave
				spawn(233721, 251.9594f, 183.4159f, 325.0038f, (byte) 30, 0, "idf5_u3_west_1");
				spawn(233722, 253.5314f, 183.5728f, 325.0038f, (byte) 30, 0, "idf5_u3_west_2");
				spawn(233722, 255.2491f, 183.4584f, 325.0038f, (byte) 30, 0, "idf5_u3_west_3");
				spawn(233721, 257.0595f, 183.5797f, 325.0045f, (byte) 30, 0, "idf5_u3_west_4");
				spawn(233737, 255.0448f, 185.5452f, 325.0038f, (byte) 30, 0, "idf5_u3_west_6");
				spawn(233725, 253.5314f, 183.5728f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_2");
				spawn(233720, 252.2491f, 183.4584f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_3");
				spawn(233731, 257.0595f, 183.5797f, 325.0045f, (byte) 30, 15000, "idf5_u3_west_4");
				spawn(233725, 258.7057f, 183.6840f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_5");
				break;
			case 702224: // south first wave
				spawn(233722, 326.3337f, 252.6159f, 291.8364f, (byte) 60, 0, "idf5_u3_south_2");
				spawn(233723, 326.3333f, 253.1857f, 291.8364f, (byte) 60, 0, "idf5_u3_south_3");
				spawn(233722, 326.4392f, 255.9983f, 291.8364f, (byte) 60, 0, "idf5_u3_south_4");
				spawn(233725, 326.3337f, 252.6159f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_2");
				spawn(233730, 326.3333f, 253.1857f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_3");
				spawn(233725, 326.4392f, 255.9983f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_4");
				spawn(233726, 326.3337f, 252.6159f, 291.8364f, (byte) 60, 30000, "idf5_u3_south_2");
				spawn(233727, 326.3333f, 253.1857f, 291.8364f, (byte) 60, 30000, "idf5_u3_south_3");
				spawn(233726, 326.4392f, 255.9983f, 291.8364f, (byte) 60, 30000, "idf5_u3_south_4");
				break;
			case 702225: // south second wave
				spawn(233722, 326.3337f, 252.6159f, 291.8364f, (byte) 60, 0, "idf5_u3_south_2");
				spawn(233723, 326.3333f, 253.1857f, 291.8364f, (byte) 60, 0, "idf5_u3_south_3");
				spawn(233722, 326.4392f, 255.9983f, 291.8364f, (byte) 60, 0, "idf5_u3_south_4");
				spawn(233725, 326.3337f, 252.6159f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_2");
				spawn(233730, 326.3333f, 253.1857f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_3");
				spawn(233725, 326.4392f, 255.9983f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_4");
				spawn(233726, 326.3337f, 252.6159f, 291.8364f, (byte) 60, 30000, "idf5_u3_south_2");
				spawn(233727, 326.3333f, 253.1857f, 291.8364f, (byte) 60, 30000, "idf5_u3_south_3");
				spawn(233726, 326.4392f, 255.9983f, 291.8364f, (byte) 60, 30000, "idf5_u3_south_4");
				break;
			case 702226: // south third wave
				spawn(233725, 326.3734f, 251.2209f, 291.8364f, (byte) 60, 0, "idf5_u3_south_1");
				spawn(233720, 326.3337f, 252.6159f, 291.8364f, (byte) 60, 0, "idf5_u3_south_2");
				spawn(233720, 326.3333f, 253.1857f, 291.8364f, (byte) 60, 0, "idf5_u3_south_3");
				spawn(233725, 326.4392f, 255.9983f, 291.8364f, (byte) 60, 0, "idf5_u3_south_4");
				spawn(233738, 324.7853f, 254.2962f, 291.8364f, (byte) 60, 0, "idf5_u3_south_6");
				spawn(233722, 326.3337f, 252.6159f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_2");
				spawn(233722, 326.3333f, 253.1857f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_3");
				spawn(233735, 326.4392f, 255.9983f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_4");
				spawn(233723, 326.4354f, 257.6836f, 291.8466f, (byte) 60, 15000, "idf5_u3_south_5");
				break;
			case 702227: // north first wave
				spawn(233722, 184.6565f, 256.3191f, 291.8364f, (byte) 0, 0, "idf5_u3_north_2");
				spawn(233727, 184.6415f, 253.7202f, 291.8364f, (byte) 0, 0, "idf5_u3_north_3");
				spawn(233722, 184.6134f, 253.0914f, 291.8364f, (byte) 0, 0, "idf5_u3_north_4");
				spawn(233725, 184.6565f, 256.3191f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_2");
				spawn(233723, 184.6415f, 253.7202f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_3");
				spawn(233725, 184.6134f, 253.0914f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_4");
				spawn(233725, 184.6565f, 256.3191f, 291.8364f, (byte) 0, 30000, "idf5_u3_north_2");
				spawn(233729, 184.6134f, 253.0914f, 291.8364f, (byte) 0, 30000, "idf5_u3_north_3");
				spawn(233725, 184.6415f, 253.7202f, 291.8364f, (byte) 0, 30000, "idf5_u3_north_4");
				spawn(233882, 253.1755f, 252.6574f, 298.2540f, (byte) 60, 30000, "idf5_u3_hide_1");
				spawn(233883, 253.1821f, 254.5660f, 298.2540f, (byte) 60, 30000, "idf5_u3_hide_2");
				spawn(233882, 253.3598f, 256.3680f, 298.2540f, (byte) 60, 30000, "idf5_u3_hide_3");
				break;
			case 702228: // north second wave
				spawn(233726, 184.6565f, 256.3191f, 291.8364f, (byte) 0, 0, "idf5_u3_north_2");
				spawn(233723, 184.6415f, 253.7202f, 291.8364f, (byte) 0, 0, "idf5_u3_north_3");
				spawn(233726, 184.6134f, 253.0914f, 291.8364f, (byte) 0, 0, "idf5_u3_north_4");
				spawn(233722, 184.6565f, 256.3191f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_2");
				spawn(233724, 184.6415f, 253.7202f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_3");
				spawn(233722, 184.6134f, 253.0914f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_4");
				spawn(233720, 184.6565f, 256.3191f, 291.8364f, (byte) 0, 30000, "idf5_u3_north_2");
				spawn(233734, 184.6415f, 253.7202f, 291.8364f, (byte) 0, 30000, "idf5_u3_north_3");
				spawn(233720, 184.6134f, 253.0914f, 291.8364f, (byte) 0, 30000, "idf5_u3_north_4");
				break;
			case 702229: // north third wave
				spawn(233725, 184.6565f, 256.3191f, 291.8364f, (byte) 0, 0, "idf5_u3_north_1");
				spawn(233720, 184.6415f, 253.7202f, 291.8364f, (byte) 0, 0, "idf5_u3_north_2");
				spawn(233724, 184.6134f, 253.0914f, 291.8364f, (byte) 0, 0, "idf5_u3_north_3");
				spawn(233725, 184.7428f, 251.3166f, 291.8842f, (byte) 0, 0, "idf5_u3_north_4");
				spawn(233731, 186.8694f, 254.6730f, 291.8364f, (byte) 0, 0, "idf5_u3_north_6");
				spawn(233722, 184.7428f, 251.3166f, 291.8842f, (byte) 0, 15000, "idf5_u3_north_2");
				spawn(233721, 184.6565f, 256.3191f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_3");
				spawn(233739, 184.6415f, 253.7202f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_4");
				spawn(233722, 184.6134f, 253.0914f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_5");
				break;
		}
	}

	private void checkGenerators() {
		ThreadPoolManager.getInstance().schedule(() -> {
			for (int id = 702220; id <= 702229; id += 3) {
				if (instance.getNpc(id) == null)
					return;
			}
			cancelTasks();
			PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_ALL_COMPLETE());

			instance.forEachNpc(npc -> npc.getController().delete());

			spawn(730886, 255.49f, 293.03f, 321.1850f, (byte) 30);
			spawn(730886, 255.49f, 215.80f, 321.2134f, (byte) 30);
			spawn(730886, 294.53f, 254.65f, 295.7718f, (byte) 60);
			spawn(730886, 216.80f, 254.65f, 295.7729f, (byte) 0);
			spawnEndboss(233740);
		}, 30000);
	}

	protected void spawnEndboss(int npcId) {
		spawn(npcId, 255.48956f, 254.5804f, 455.1201f, (byte) 15);
	}

	protected void wipe() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (isInstanceDestroyed)
				return;
			instance.forEach(o -> {
				if (o instanceof Npc npc)
					npc.getController().delete();
				else if (o instanceof Player p && !p.isDead())
					p.getController().die();
			});
		}, 5000);
	}

	protected void spawn(int npcId, float x, float y, float z, byte h, int delay, String walkerId) {
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed) {
				Npc npc = (Npc) spawn(npcId, x, y, z, h);
				npc.getSpawn().setWalkerId(walkerId);
				tasks.add(ThreadPoolManager.getInstance().schedule(() -> WalkManager.startWalking((NpcAI) npc.getAi()), 2500));
			}
		}, delay));
	}

	private void cancelTasks() {
		tasks.stream().filter(t -> t != null && !t.isCancelled()).forEach(t -> t.cancel(true));
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 730886:
				TeleportService.teleportTo(player, instance, 265.45142f, 264.52875f, 455.1256f, (byte) 75);
				break;
			case 702009:
				SkillEngine.getInstance().getSkill(npc, 21511, 1, player).useSkill();
				TeleportService.teleportTo(player, instance, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), TeleportAnimation.FADE_OUT_BEAM);
				npc.getController().delete();
				break;
			case 730905:
				TeleportService.moveToInstanceExit(player, mapId, player.getRace());
				break;
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		// TODO: movie id PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 0, ???, 0));
		if (isRaceSet.compareAndSet(false, true)) {
			int npcId = player.getRace() == Race.ASMODIANS ? 802049 : 802048;
			spawn(npcId, 315.74573f, 306.9366f, 405.49997f, (byte) 15);
		}
	}

	@Override
	public void onEndEffect(Effect effect) {
		if (effect.getSkillId() == 21511) {
			Creature effected = effect.getEffected();
			spawn(702009, effected.getX(), effected.getY(), effected.getZ(), effected.getHeading());
		}
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		int npcId = npc.getNpcId();
		if (npcId == 233740 || npcId == 234686)
			spawn(730905, 267.64062f, 267.84793f, 276.65512f, (byte) 75); // exit
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, true, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		TeleportService.teleportTo(player, instance, 271.1714f, 271.4455f, 276.67294f, (byte) 75);
		return true;
	}

	@Override
	public void leaveInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
		cancelTasks();
	}

	@Override
	public boolean isBoss(Npc npc) {
		return npc.getNpcId() == 233740 || npc.getNpcId() == 234686; // Dynatoum
	}
}
