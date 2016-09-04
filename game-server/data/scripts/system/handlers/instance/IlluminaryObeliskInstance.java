package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

import javolution.util.FastTable;

/**
 * @author M.O.G. Dision
 * @reworked Estrayl
 */
@InstanceID(301230000)
public class IlluminaryObeliskInstance extends GeneralInstanceHandler {

	private AtomicBoolean isRaceSet = new AtomicBoolean(false);
	public Map<Integer, StaticDoor> doors;
	private List<Future<?>> spawnTasks = new FastTable<>();
	private Future<?> generatorCheckTask;
	protected Future<?> wipeTask;
	public boolean isInstanceDestroyed;
	protected int wipeMsgProgress = 1402129;
	protected byte wipeProgress = 0;
	
	protected int getBossId() {
		return 233740;
	}

	@Override
	public void onInstanceCreate(WorldMapInstance wmi) {
		super.onInstanceCreate(wmi);
		doors = wmi.getDoors();
		scheduleInstanceStart();
	}
	
	private void scheduleInstanceStart() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				sendMsg(1402193);
				doors.get(129).setOpen(true);
				scheduleWipeTask();
				scheduleGeneratorCheck();
				spawn(702014, 343.1202f, 254.10585f, 291.62302f, (byte) 0, 34); //Invasion Corridors
				spawn(702015, 169.5563f, 254.52907f, 293.04276f, (byte) 0, 17);
				spawn(702016, 255.7034f, 171.83853f, 325.81653f, (byte) 0, 18);
				spawn(702017, 255.7926f, 338.22058f, 325.56473f, (byte) 0, 60);
			}
		}, 60000);
	}

	protected void scheduleWipeTask() {
		wipeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					switch (wipeProgress) {
						case 0: //30min announce
						case 5: //25min announce
						case 10://20min announce
						case 15://15min announce
						case 20://10min announce
						case 25:// 5min announce
							sendMsg(wipeMsgProgress++);
							if (wipeProgress != 0) //no sense for assault on start
								scheduleAdditionalAttack(Rnd.get(1, 4));
							break;
						case 29:// 1min announce
							sendMsg(1402235);
							break;
						case 30:// wipe
							sendMsg(1402236);
							wipe();
							break;
					}
					wipeProgress++;
				}
			}
		}, 0, 60 * 1000); //Repeat every minute
	}
	
	private void scheduleGeneratorCheck() {
		generatorCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!isInstanceDestroyed)
					checkGenerators();
			}
		}, 1000, 15000);
	}
	
	private void checkGenerators() {
		Npc gen1 = instance.getNpc(702220);
		Npc gen2 = instance.getNpc(702223);
		Npc gen3 = instance.getNpc(702229);
		Npc gen4 = instance.getNpc(702226);
		if (gen1 == null || gen2 == null || gen3 == null || gen4 == null)
			return;
		
		cancelTaskList();
		cancelSingleTask(generatorCheckTask);
		cancelSingleTask(wipeTask);
		sendMsg(1402202); // Portal
		for (Npc npc : instance.getNpcs()) {
			npc.getController().onDelete();
		}
		spawn(730886, 255.49f, 293.3f, 321.18497f, (byte) 30);
		spawn(730886, 255.49f, 215.8f, 321.21344f, (byte) 30);
		spawn(730886, 294.53f, 254.65f, 295.77176f, (byte) 60);
		spawn(730886, 216.8f, 254.65f, 295.7729f, (byte) 0);
		spawn(getBossId(), 255.48956f, 254.5804f, 455.1201f, (byte) 15);
	}

	protected void wipe() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (isInstanceDestroyed)
					return;
				instance.forEachPlayer(new Visitor<Player>() {
					@Override
					public void visit(Player pl) {
						if (!pl.getLifeStats().isAlreadyDead())
							pl.getController().onAttack(pl, pl.getLifeStats().getMaxHp() + 1, true);
					}
				});
				onInstanceDestroy();
			}
		}, 5000);
	}

	protected void sp(final int npcId, final float x, final float y, final float z, final byte h, final int delay, final String walkerId) {
		Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					Npc npc = (Npc) spawn(npcId, x, y, z, h);
					npc.getSpawn().setWalkerId(walkerId);
					WalkManager.startWalking((NpcAI2) npc.getAi2());
					npc.setState(CreatureState.WALKING);
					PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
				}
			}
		}, delay);
		spawnTasks.add(task);
	}
	
	protected void scheduleAdditionalAttack(int locationId) {
		switch (locationId) {
			case 1: //North
				sp(233723, 181.01f, 257.40f, 291.83f, (byte) 119, 2000, "4_left_301230000");
				sp(233724, 180.83f, 252.54f, 291.83f, (byte) 119, 2500, "4_right_301230000");
				sp(233725, 183.05f, 254.72f, 291.83f, (byte) 119, 3000, "4_center_301230000");
				sp(233726, 181.01f, 257.40f, 291.83f, (byte) 119, 11000, "4_left_301230000");
				sp(233727, 180.83f, 252.54f, 291.83f, (byte) 119, 11500, "4_right_301230000");
				sp(233728, 183.05f, 254.72f, 291.83f, (byte) 119, 12000, "4_center_301230000");
				sp(233729, 181.01f, 257.40f, 291.83f, (byte) 119, 21000, "4_left_301230000");
				sp(233722, 180.83f, 252.54f, 291.83f, (byte) 119, 21500, "4_right_301230000");
				sp(233721, 183.05f, 254.72f, 291.83f, (byte) 119, 22000, "4_center_301230000");
				sp(233723, 181.01f, 257.40f, 291.83f, (byte) 119, 81000, "4_left_301230000");
				sp(233724, 180.83f, 252.54f, 291.83f, (byte) 119, 81500, "4_right_301230000");
				sp(233725, 183.05f, 254.72f, 291.83f, (byte) 119, 82000, "4_center_301230000");
				sp(233726, 181.01f, 257.40f, 291.83f, (byte) 119, 91000, "4_left_301230000");
				sp(233727, 180.83f, 252.54f, 291.83f, (byte) 119, 91500, "4_right_301230000");
				sp(233728, 183.05f, 254.72f, 291.83f, (byte) 119, 92000, "4_center_301230000");
				sp(233729, 181.01f, 257.40f, 291.83f, (byte) 119, 101000, "4_left_301230000");
				sp(233722, 180.83f, 252.54f, 291.83f, (byte) 119, 101500, "4_right_301230000");
				sp(233721, 183.05f, 254.72f, 291.83f, (byte) 119, 102000, "4_center_301230000");
				break;
			case 2: //South
				sp(233738, 329.78f, 251.68f, 291.83f, (byte) 60, 2000, "3_left_301230000");
				sp(233739, 329.84f, 256.80f, 291.83f, (byte) 60, 2500, "3_right_301230000");
				sp(233730, 328.09f, 254.24f, 291.83f, (byte) 60, 3000, "3_center_301230000");
				sp(233731, 329.78f, 251.68f, 291.83f, (byte) 60, 11000, "3_left_301230000");
				sp(233732, 329.84f, 256.80f, 291.83f, (byte) 60, 11500, "3_right_301230000");
				sp(233733, 328.09f, 254.24f, 291.83f, (byte) 60, 12000, "3_center_301230000");
				sp(233734, 329.78f, 251.68f, 291.83f, (byte) 60, 21000, "3_left_301230000");
				sp(233735, 329.84f, 256.80f, 291.83f, (byte) 60, 21500, "3_right_301230000");
				sp(233736, 328.09f, 254.24f, 291.83f, (byte) 60, 22000, "3_center_301230000");
				sp(233738, 329.78f, 251.68f, 291.83f, (byte) 60, 81000, "3_left_301230000");
				sp(233739, 329.84f, 256.80f, 291.83f, (byte) 60, 81500, "3_right_301230000");
				sp(233730, 328.09f, 254.24f, 291.83f, (byte) 60, 82000, "3_center_301230000");
				sp(233731, 329.78f, 251.68f, 291.83f, (byte) 60, 91000, "3_left_301230000");
				sp(233732, 329.84f, 256.80f, 291.83f, (byte) 60, 91500, "3_right_301230000");
				sp(233733, 328.09f, 254.24f, 291.83f, (byte) 60, 92000, "3_center_301230000");
				sp(233734, 329.78f, 251.68f, 291.83f, (byte) 60, 101000, "3_left_301230000");
				sp(233735, 329.84f, 256.80f, 291.83f, (byte) 60, 101500, "3_right_301230000");
				sp(233736, 328.09f, 254.24f, 291.83f, (byte) 60, 102000, "3_center_301230000");
				break;
			case 3: //West
				sp(233729, 253.31f, 180.35f, 325.00f, (byte) 30, 2000, "2_left_301230000");
				sp(233730, 257.56f, 180.41f, 325.00f, (byte) 30, 2500, "2_right_301230000");
				sp(233731, 255.39f, 182.25f, 325.00f, (byte) 30, 3000, "2_center_301230000");
				sp(233732, 253.31f, 180.35f, 325.00f, (byte) 30, 11000, "2_left_301230000");
				sp(233733, 257.56f, 180.41f, 325.00f, (byte) 30, 11500, "2_right_301230000");
				sp(233734, 255.39f, 182.25f, 325.00f, (byte) 30, 12000, "2_center_301230000");
				sp(233735, 253.31f, 180.35f, 325.00f, (byte) 30, 21000, "2_left_301230000");
				sp(233736, 257.56f, 180.41f, 325.00f, (byte) 30, 21500, "2_right_301230000");
				sp(233737, 255.39f, 182.25f, 325.00f, (byte) 30, 22000, "2_center_301230000");
				sp(233729, 253.31f, 180.35f, 325.00f, (byte) 30, 81000, "2_left_301230000");
				sp(233730, 257.56f, 180.41f, 325.00f, (byte) 30, 81500, "2_right_301230000");
				sp(233731, 255.39f, 182.25f, 325.00f, (byte) 30, 82000, "2_center_301230000");
				sp(233732, 253.31f, 180.35f, 325.00f, (byte) 30, 91000, "2_left_301230000");
				sp(233733, 257.56f, 180.41f, 325.00f, (byte) 30, 91500, "2_right_301230000");
				sp(233734, 255.39f, 182.25f, 325.00f, (byte) 30, 92000, "2_center_301230000");
				sp(233735, 253.31f, 180.35f, 325.00f, (byte) 30, 101000, "2_left_301230000");
				sp(233736, 257.56f, 180.41f, 325.00f, (byte) 30, 101500, "2_right_301230000");
				sp(233737, 255.39f, 182.25f, 325.00f, (byte) 30, 102000, "2_center_301230000");
				break;
			case 4: //East
				sp(233720, 257.31f, 328.03f, 325.00f, (byte) 91, 2000, "1_left_301230000");
				sp(233721, 253.57f, 328.10f, 325.00f, (byte) 91, 2500, "1_right_301230000");
				sp(233722, 255.40f, 326.54f, 325.00f, (byte) 91, 3000, "1_center_301230000");
				sp(233723, 257.31f, 328.03f, 325.00f, (byte) 91, 11000, "1_left_301230000");
				sp(233724, 253.57f, 328.10f, 325.00f, (byte) 91, 11500, "1_right_301230000");
				sp(233725, 255.40f, 326.54f, 325.00f, (byte) 91, 12000, "1_center_301230000");
				sp(233726, 257.31f, 328.03f, 325.00f, (byte) 91, 21000, "1_left_301230000");
				sp(233727, 253.57f, 328.10f, 325.00f, (byte) 91, 21500, "1_right_301230000");
				sp(233728, 255.40f, 326.54f, 325.00f, (byte) 91, 22000, "1_center_301230000");
				sp(233720, 257.31f, 328.03f, 325.00f, (byte) 91, 81000, "1_left_301230000");
				sp(233721, 253.57f, 328.10f, 325.00f, (byte) 91, 81500, "1_right_301230000");
				sp(233722, 255.40f, 326.54f, 325.00f, (byte) 91, 82000, "1_center_301230000");
				sp(233723, 257.31f, 328.03f, 325.00f, (byte) 91, 91000, "1_left_301230000");
				sp(233724, 253.57f, 328.10f, 325.00f, (byte) 91, 91500, "1_right_301230000");
				sp(233725, 255.40f, 326.54f, 325.00f, (byte) 91, 92000, "1_center_301230000");
				sp(233726, 257.31f, 328.03f, 325.00f, (byte) 91, 101000, "1_left_301230000");
				sp(233727, 253.57f, 328.10f, 325.00f, (byte) 91, 101500, "1_right_301230000");
				sp(233728, 255.40f, 326.54f, 325.00f, (byte) 91, 102000, "1_center_301230000");
				break;
		}
	}
	
	private void cancelTaskList() {
		for (Future<?> task : spawnTasks) {
			cancelSingleTask(task);
		}
	}

	private void cancelSingleTask(Future<?> task) {
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 730886:
				TeleportService2.teleportTo(player, mapId, instanceId, 265.45142f, 264.52875f, 455.1256f, (byte) 75);
				break;
			case 702009:
				TeleportService2.teleportTo(player, mapId, instanceId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), TeleportAnimation.FADE_OUT_BEAM);
				SkillEngine.getInstance().applyEffectDirectly(21511, player, player, 0);
				npc.getController().onDelete();
				break;
			case 730905:
				TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
				break;
		}
	}
	
	@Override
	public void onEnterInstance(Player player) {
		// PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 0, ???, 0));
		if (isRaceSet.compareAndSet(false, true)) {
			int npcId = player.getRace() == Race.ASMODIANS ? 802049 : 802048;
			spawn(npcId, 315.74573f, 306.9366f, 405.49997f, (byte) 15);
		}
	}
	
	@Override
	public void onEndEffect(Creature effector, Creature effected, int skillId) {
		if (skillId == 21511)
			spawn(702009, effected.getX(), effected.getY(), effected.getZ(), effected.getHeading());
	}
	
	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));
		return true;
	}
	
	@Override
	public void onDie(Npc npc) {
		int npcId = npc.getNpcId();
		if (npcId >= 233720 && npcId <= 233739)
			npc.getController().onDelete();
		if (npcId == getBossId())
			spawn(730905, 267.64062f, 267.84793f, 276.65512f, (byte) 75); // exit
	}
	
	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		TeleportService2.teleportTo(player, mapId, instanceId, 271.1714f, 271.4455f, 276.67294f, (byte) 75);
		return true;
	}
	
	@Override
	public void onInstanceDestroy() {
		cancelSingleTask(generatorCheckTask);
		cancelSingleTask(wipeTask);
		cancelTaskList();
		doors.clear();
		for (Npc npc : instance.getNpcs()) {
			npc.getController().onDelete();
		}
		isInstanceDestroyed = true;
	}
	
	@Override
	public void onExitInstance(Player player) {
		TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
	}
}