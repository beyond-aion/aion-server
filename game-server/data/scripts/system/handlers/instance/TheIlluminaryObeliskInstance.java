package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javolution.util.FastTable;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TeleportAnimation;
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
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author M.O.G. Dision
 */
@InstanceID(301230000)
public class TheIlluminaryObeliskInstance extends GeneralInstanceHandler {

	public Map<Integer, StaticDoor> doors;
	private List<Npc> npcs = new FastTable<Npc>();
	private Race spawnRace;
	private Future<?> cancelTasks;
	public boolean isInstanceDestroyed;
	public boolean isCancelled;
	private Future<?> timeCheckTask;
	private Future<?> failTask;
	private byte timeInMin = -1;
	private AtomicBoolean isStartFail = new AtomicBoolean(false);

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		doors = instance.getDoors();
		generatorTaskStart();

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sendMsg(1402193);
				doors.get(129).setOpen(true);
			}
		}, 60000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				StartTimer();
			}
		}, 70000);
	}

	@Override
	public void onEnterInstance(Player player) {
		// PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 0, ???, 0));
		if (spawnRace == null) {
			spawnRace = player.getRace();
			SpawnRace();
		}
	}

	@Override
	public void onAggro(Npc npc) {
		switch (npc.getNpcId()) {
			case 233740:
				if (isStartFail.compareAndSet(false, true)) {
					sendMsg(1402143);
					startFailTask();
				}
				break;
		}
	}

	private void startFailTask() {
		failTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				timeInMin++;
				switch (timeInMin) {
					case 1:
						sendMsg(1402144);
						break;
					case 5:
						sendMsg(1402145);
						break;
					case 6:
						sendMsg(1402146);
						Npc boss = instance.getNpc(233740);
						boss.getController().onDelete();
						spawn(730905, 267.64062f, 267.84793f, 276.65512f, (byte) 75); // exit
						if (failTask != null && !failTask.isDone()) {
							failTask.cancel(true);
						}
						break;
				}
			}
		}, 0, 60000);
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 233720: // Vritra's Legion Despawn
			case 233721:
			case 233722:
			case 233723:
			case 233724:
			case 233725:
			case 233726:
			case 233727:
			case 233728:
			case 233729:
			case 233730:
			case 233731:
			case 233732:
			case 233733:
			case 233734:
			case 233735:
			case 233736:
			case 233737:
			case 233738:
			case 233739:
				despawnNpc(npc);
				break;
			case 233740:
				if (failTask != null && !failTask.isDone()) {
					failTask.cancel(true);
				}
				spawn(730905, 267.64062f, 267.84793f, 276.65512f, (byte) 75); // exit
				break;

		}
	}

	private void SpawnRace() {
		// NPC's
		final int npcs = spawnRace == Race.ASMODIANS ? 802049 : 802048;
		spawn(npcs, 315.74573f, 306.9366f, 405.49997f, (byte) 15); // NPC
	}

	public void cancelTask() {
		if (cancelTasks != null && !cancelTasks.isCancelled()) {
			cancelTasks.cancel(true);
		}
	}

	private void StartTimer() {

		cancelTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					sendMsg(1402129); // 30 min.
				}
			}
		}, 1000);

		cancelTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					sendMsg(1402130); // 25 min
				}
			}
		}, 70000);

		cancelTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					sendMsg(1402131); // 20 min
					phaseAttack01();
					phaseAttack02();
				}
			}
		}, 900000);

		cancelTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					sendMsg(1402132); // 15 min
					phaseAttack03();
					phaseAttack04();
				}
			}
		}, 1200000);

		cancelTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					sendMsg(1402133); // 10 min
					phaseAttack01();
					phaseAttack02();
					phaseAttack03();
					phaseAttack04();
				}
			}
		}, 1500000);

		cancelTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					sendMsg(1402134); // 5 min
				}
			}
		}, 1800000);

		cancelTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					megaAttack();
				}
			}
		}, 1810000);
	}

	private void generatorTaskStart() {
		timeCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				checkGenerators();
			}
		}, 1000, 30000);
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 730886:
				TeleportService2.teleportTo(player, mapId, instanceId, 265.45142f, 264.52875f, 455.1256f, (byte) 75);
				break;
			case 702009:
				TeleportService2
					.teleportTo(player, mapId, instanceId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), TeleportAnimation.BEAM_ANIMATION);
				SkillEngine.getInstance().applyEffectDirectly(21511, player, player, 0);
				npc.getController().onDelete();
				break;
			case 730905:
				TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
				break;

		}
	}

	@Override
	public void onEndEffect(Creature effector, Creature effected, int skillId) {
		switch (skillId) {
			case 21511:
				spawn(702009, effected.getX(), effected.getY(), effected.getZ(), effected.getHeading());
				break;
		}
	}

	private boolean checkGenerators() {
		Npc gen1 = instance.getNpc(702220);
		Npc gen2 = instance.getNpc(702223);
		Npc gen3 = instance.getNpc(702229);
		Npc gen4 = instance.getNpc(702226);
		if (gen1 != null && gen2 != null && gen3 != null && gen4 != null) {
			cancelTask();
			if (timeCheckTask != null && !timeCheckTask.isCancelled())
				timeCheckTask.cancel(true);
			sendMsg(1402202); // Portal
			for (Npc npc : instance.getNpcs()) {
				npc.getController().onDelete();
			}
			spawn(730886, 255.49f, 293.3f, 321.18497f, (byte) 30);
			spawn(730886, 255.49f, 215.8f, 321.21344f, (byte) 30);
			spawn(730886, 294.53f, 254.65f, 295.77176f, (byte) 60);
			spawn(730886, 216.8f, 254.65f, 295.7729f, (byte) 0);
			spawn(233740, 255.48956f, 254.5804f, 455.1201f, (byte) 15);
			return true;
		}
		return false;
	}

	private void megaAttack() {

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sendMsg(1402236);
			}
		}, 1000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {

				instance.doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player pl) {

						if (pl instanceof Creature) {
							Creature creature = pl;
							creature.getController().onAttack(pl, creature.getLifeStats().getMaxHp() + 1, true);
						}
					}
				});
			}
		}, 5000);
	}

	private void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time, final String walkerId,
		final boolean isRun) {

		cancelTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					Npc npc = (Npc) spawn(npcId, x, y, z, h);
					npcs.add(npc);
					npc.getSpawn().setWalkerId(walkerId);
					WalkManager.startWalking((NpcAI2) npc.getAi2());
					if (isRun) {
						npc.setState(1);
					} else {
						npc.setState(CreatureState.WALKING);
					}
					PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
				}
			}
		}, time);
	}

	private void phaseAttack01() {

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233720, 257.31f, 328.03f, 325.00f, (byte) 91, 1000, "1_left_301230000", false);
				sp(233721, 253.57f, 328.10f, 325.00f, (byte) 91, 1500, "1_right_301230000", false);
				sp(233722, 255.40f, 326.54f, 325.00f, (byte) 91, 2000, "1_center_301230000", false);
			}
		}, 1000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233723, 257.31f, 328.03f, 325.00f, (byte) 91, 1000, "1_left_301230000", false);
				sp(233724, 253.57f, 328.10f, 325.00f, (byte) 91, 1500, "1_right_301230000", false);
				sp(233725, 255.40f, 326.54f, 325.00f, (byte) 91, 2000, "1_center_301230000", false);
			}
		}, 10000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233726, 257.31f, 328.03f, 325.00f, (byte) 91, 1000, "1_left_301230000", false);
				sp(233727, 253.57f, 328.10f, 325.00f, (byte) 91, 1500, "1_right_301230000", false);
				sp(233728, 255.40f, 326.54f, 325.00f, (byte) 91, 2000, "1_center_301230000", false);
			}
		}, 20000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233720, 257.31f, 328.03f, 325.00f, (byte) 91, 1000, "1_left_301230000", false);
				sp(233721, 253.57f, 328.10f, 325.00f, (byte) 91, 1500, "1_right_301230000", false);
				sp(233722, 255.40f, 326.54f, 325.00f, (byte) 91, 2000, "1_center_301230000", false);
			}
		}, 80000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233723, 257.31f, 328.03f, 325.00f, (byte) 91, 1000, "1_left_301230000", false);
				sp(233724, 253.57f, 328.10f, 325.00f, (byte) 91, 1500, "1_right_301230000", false);
				sp(233725, 255.40f, 326.54f, 325.00f, (byte) 91, 2000, "1_center_301230000", false);
			}
		}, 90000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233726, 257.31f, 328.03f, 325.00f, (byte) 91, 1000, "1_left_301230000", false);
				sp(233727, 253.57f, 328.10f, 325.00f, (byte) 91, 1500, "1_right_301230000", false);
				sp(233728, 255.40f, 326.54f, 325.00f, (byte) 91, 2000, "1_center_301230000", false);
			}
		}, 100000);
	}

	private void phaseAttack02() {

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233729, 253.31f, 180.35f, 325.00f, (byte) 30, 1000, "2_left_301230000", false);
				sp(233730, 257.56f, 180.41f, 325.00f, (byte) 30, 1500, "2_right_301230000", false);
				sp(233731, 255.39f, 182.25f, 325.00f, (byte) 30, 2000, "2_center_301230000", false);
			}
		}, 1000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233732, 253.31f, 180.35f, 325.00f, (byte) 30, 1000, "2_left_301230000", false);
				sp(233733, 257.56f, 180.41f, 325.00f, (byte) 30, 1500, "2_right_301230000", false);
				sp(233734, 255.39f, 182.25f, 325.00f, (byte) 30, 2000, "2_center_301230000", false);
			}
		}, 10000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233735, 253.31f, 180.35f, 325.00f, (byte) 30, 1000, "2_left_301230000", false);
				sp(233736, 257.56f, 180.41f, 325.00f, (byte) 30, 1500, "2_right_301230000", false);
				sp(233737, 255.39f, 182.25f, 325.00f, (byte) 30, 2000, "2_center_301230000", false);
			}
		}, 20000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233729, 253.31f, 180.35f, 325.00f, (byte) 30, 1000, "2_left_301230000", false);
				sp(233730, 257.56f, 180.41f, 325.00f, (byte) 30, 1500, "2_right_301230000", false);
				sp(233731, 255.39f, 182.25f, 325.00f, (byte) 30, 2000, "2_center_301230000", false);
			}
		}, 80000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233732, 253.31f, 180.35f, 325.00f, (byte) 30, 1000, "2_left_301230000", false);
				sp(233733, 257.56f, 180.41f, 325.00f, (byte) 30, 1500, "2_right_301230000", false);
				sp(233734, 255.39f, 182.25f, 325.00f, (byte) 30, 2000, "2_center_301230000", false);
			}
		}, 90000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233735, 253.31f, 180.35f, 325.00f, (byte) 30, 1000, "2_left_301230000", false);
				sp(233736, 257.56f, 180.41f, 325.00f, (byte) 30, 1500, "2_right_301230000", false);
				sp(233737, 255.39f, 182.25f, 325.00f, (byte) 30, 2000, "2_center_301230000", false);
			}
		}, 100000);
	}

	private void phaseAttack03() {

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233738, 329.78f, 251.68f, 291.83f, (byte) 60, 1000, "3_left_301230000", false);
				sp(233739, 329.84f, 256.80f, 291.83f, (byte) 60, 1500, "3_right_301230000", false);
				sp(233730, 328.09f, 254.24f, 291.83f, (byte) 60, 2000, "3_center_301230000", false);
			}
		}, 1000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233731, 329.78f, 251.68f, 291.83f, (byte) 60, 1000, "3_left_301230000", false);
				sp(233732, 329.84f, 256.80f, 291.83f, (byte) 60, 1500, "3_right_301230000", false);
				sp(233733, 328.09f, 254.24f, 291.83f, (byte) 60, 2000, "3_center_301230000", false);
			}
		}, 10000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233734, 329.78f, 251.68f, 291.83f, (byte) 60, 1000, "3_left_301230000", false);
				sp(233735, 329.84f, 256.80f, 291.83f, (byte) 60, 1500, "3_right_301230000", false);
				sp(233736, 328.09f, 254.24f, 291.83f, (byte) 60, 2000, "3_center_301230000", false);
			}
		}, 20000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233738, 329.78f, 251.68f, 291.83f, (byte) 60, 1000, "3_left_301230000", false);
				sp(233739, 329.84f, 256.80f, 291.83f, (byte) 60, 1500, "3_right_301230000", false);
				sp(233730, 328.09f, 254.24f, 291.83f, (byte) 60, 2000, "3_center_301230000", false);
			}
		}, 80000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233731, 329.78f, 251.68f, 291.83f, (byte) 60, 1000, "3_left_301230000", false);
				sp(233732, 329.84f, 256.80f, 291.83f, (byte) 60, 1500, "3_right_301230000", false);
				sp(233733, 328.09f, 254.24f, 291.83f, (byte) 60, 2000, "3_center_301230000", false);
			}
		}, 90000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233734, 329.78f, 251.68f, 291.83f, (byte) 60, 1000, "3_left_301230000", false);
				sp(233735, 329.84f, 256.80f, 291.83f, (byte) 60, 1500, "3_right_301230000", false);
				sp(233736, 328.09f, 254.24f, 291.83f, (byte) 60, 2000, "3_center_301230000", false);
			}
		}, 100000);
	}

	private void phaseAttack04() {

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233723, 181.01f, 257.40f, 291.83f, (byte) 119, 1000, "4_left_301230000", false);
				sp(233724, 180.83f, 252.54f, 291.83f, (byte) 119, 1500, "4_right_301230000", false);
				sp(233725, 183.05f, 254.72f, 291.83f, (byte) 119, 2000, "4_center_301230000", false);
			}
		}, 1000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233726, 181.01f, 257.40f, 291.83f, (byte) 119, 1000, "4_left_301230000", false);
				sp(233727, 180.83f, 252.54f, 291.83f, (byte) 119, 1500, "4_right_301230000", false);
				sp(233728, 183.05f, 254.72f, 291.83f, (byte) 119, 2000, "4_center_301230000", false);
			}
		}, 10000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233729, 181.01f, 257.40f, 291.83f, (byte) 119, 1000, "4_left_301230000", false);
				sp(233722, 180.83f, 252.54f, 291.83f, (byte) 119, 1500, "4_right_301230000", false);
				sp(233721, 183.05f, 254.72f, 291.83f, (byte) 119, 2000, "4_center_301230000", false);
			}
		}, 20000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233723, 181.01f, 257.40f, 291.83f, (byte) 119, 1000, "4_left_301230000", false);
				sp(233724, 180.83f, 252.54f, 291.83f, (byte) 119, 1500, "4_right_301230000", false);
				sp(233725, 183.05f, 254.72f, 291.83f, (byte) 119, 2000, "4_center_301230000", false);
			}
		}, 80000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233726, 181.01f, 257.40f, 291.83f, (byte) 119, 1000, "4_left_301230000", false);
				sp(233727, 180.83f, 252.54f, 291.83f, (byte) 119, 1500, "4_right_301230000", false);
				sp(233728, 183.05f, 254.72f, 291.83f, (byte) 119, 2000, "4_center_301230000", false);
			}
		}, 90000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233729, 181.01f, 257.40f, 291.83f, (byte) 119, 1000, "4_left_301230000", false);
				sp(233722, 180.83f, 252.54f, 291.83f, (byte) 119, 1500, "4_right_301230000", false);
				sp(233721, 183.05f, 254.72f, 291.83f, (byte) 119, 2000, "4_center_301230000", false);
			}
		}, 100000);
	}

	private void despawnNpc(Npc npc) {
		if (npc != null) {
			npc.getController().onDelete();
		}
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));

		return true;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME);
		TeleportService2.teleportTo(player, mapId, instanceId, 271.1714f, 271.4455f, 276.67294f, (byte) 75);
		return true;
	}

	@Override
	public void onInstanceDestroy() {
		cancelTask();
		isCancelled = true;
		isInstanceDestroyed = true;
		doors.clear();
		if (failTask != null && !failTask.isDone()) {
			failTask.cancel(true);
		}
		if (timeCheckTask != null && !timeCheckTask.isCancelled())
			timeCheckTask.cancel(true);
	}

	@Override
	public void onExitInstance(Player player) {
		TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
	}
}
