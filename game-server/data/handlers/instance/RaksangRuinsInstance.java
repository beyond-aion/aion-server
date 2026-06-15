package instance;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(300610000)
public class RaksangRuinsInstance extends GeneralInstanceHandler {

	private final int way = Rnd.get(0, 2);
	private final AtomicInteger spawns = new AtomicInteger();
	private final AtomicBoolean isEventStarted = new AtomicBoolean();
	private final AtomicInteger waveKillz = new AtomicInteger();
	private volatile boolean isInstanceDestroyed;
	private Future<?> spawnTask;
	private boolean isDoorAccessible = false;

	public RaksangRuinsInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public float getExpMultiplier() {
		return 1.8f;
	}

	@Override
	public void onEnterInstance(Player player) {
		spawn(206378 + way + (player.getRace() == Race.ASMODIANS ? 17 : 0), 818.103f, 931.0215f, 1207.4312f, (byte) 13);
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 236010: // Trained Porgus
			case 236011: // Trained Worg
			case 236012: // Crumbling Skelesword
			case 236013: // Withering Husk
			case 236014: // Ragelich Adept
				if (waveKillz.incrementAndGet() >= 31) {
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_A_END());
					isDoorAccessible = true;
				}
				break;
			case 236074:
			case 236075:
			case 236076:
				switch (waveKillz.incrementAndGet()) {
					case 15:
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_C_END());
						instance.setDoorState(457, true);
						break;
					case 30:
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_C_END());
						instance.setDoorState(64, true);
						break;
				}
				break;
			case 236020:
			case 236021:
			case 236096:
			case 236097:
				switch (waveKillz.incrementAndGet()) {
					case 28:
						spawnTask.cancel(false);
						spawnTask = null;
						spawns.set(0);
						instance.setDoorState(107, true);
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_B_END());
						break;
					case 56:
						spawn(236305, 331.107f, 787.326f, 147.798f, (byte) 47);
						break;
				}
				break;
			case 236084:
				instance.setDoorState(307, true);
				break;
			case 217469:
				instance.setDoorState(107, true);
				break;
			case 236303:
				instance.setDoorState(294, true);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_A_END());
				break;
			case 236304:
				instance.setDoorState(118, true);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_C_END());
				break;
			case 236305:
				instance.setDoorState(324, true);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_B_END());
				break;
			case 236306:
				spawn(730445, 620.65f, 663.44f, 522.049f, (byte) 27); // Exit
				break;
			case 0:
				spawn(730438, 628.132f, 187.505f, 924.86f, (byte) 0);
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 702673: // Torment's Forge
			case 702674:
			case 702675:
			case 702690:
			case 702691:
			case 702692:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_A_START());
				delaySpawn(player, 236074 + Rnd.nextInt(3), 2000);
				delaySpawn(player, 236074 + Rnd.nextInt(3), 2000);
				delaySpawn(player, 236074 + Rnd.nextInt(3), 8000);
				delaySpawn(player, 236074 + Rnd.nextInt(3), 8000);
				delaySpawn(player, 236074 + Rnd.nextInt(3), 12000);
				npc.getController().delete();
				break;
			case 730438: // Terror' Vault
				if (isDoorAccessible)
					TeleportService.teleportTo(player, instance, 711.10895f, 312.82013f, 910.6781f);
				else
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_A_DOOR_CONDITION());
				break;
		}
	}

	/**
	 * Used for way A (Terror's Vault & Cuttling Grounds).
	 */
	private void delaySpawn(int npcId, int delay) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed)
				spawn(npcId, 621.50f, 198.54f, 924.838f, (byte) 42);
		}, delay);
	}

	private void delaySpawn(byte step, int delay) {
		spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (spawns.incrementAndGet() > 8 || isInstanceDestroyed) {
				spawnTask.cancel(false);
				return;
			}
			switch (step) {
				case 1:
					switch (spawns.get()) {
						case 1:
						case 3:
						case 5:
						case 7:
							spawn(236020, 344.016f, 630.834f, 146.514f, (byte) 0);
							spawn(236020, 325.803f, 645.552f, 146.514f, (byte) 0);
							spawn(236020, 307.303f, 614.676f, 146.514f, (byte) 0);
							spawn(236020, 329.768f, 608.466f, 146.514f, (byte) 0);
							break;
						case 2:
						case 4:
						case 6:
						case 8:
							spawn(236021, 312.577f, 605.469f, 146.514f, (byte) 0);
							spawn(236021, 307.422f, 630.051f, 146.514f, (byte) 0);
							spawn(236021, 337.200f, 639.658f, 146.514f, (byte) 0);
							break;
					}
					break;
				case 2:
					switch (spawns.get()) {
						case 1:
						case 3:
						case 5:
						case 7:
							spawn(236096, 303.755f, 772.090f, 148.988f, (byte) 0);
							spawn(236096, 350.070f, 801.111f, 145.082f, (byte) 0);
							spawn(236096, 332.003f, 767.004f, 147.394f, (byte) 0);
							spawn(236096, 325.803f, 810.720f, 144.885f, (byte) 0);
							break;
						case 2:
						case 4:
						case 6:
						case 8:
							spawn(236097, 344.371f, 781.986f, 147.159f, (byte) 0);
							spawn(236097, 339.273f, 813.446f, 145.390f, (byte) 0);
							spawn(236097, 317.126f, 766.019f, 148.844f, (byte) 0);
							break;
					}
					break;
				case 3:
					switch (spawns.get()) {
						case 1:
							delaySpawn(236010, 1000);
							delaySpawn(236010, 4000);
							delaySpawn(236010, 7000);
							delaySpawn(236010, 10000);
							delaySpawn(236010, 13000);
							break;
						case 2:
							delaySpawn(236010, 1000);
							delaySpawn(236010, 3000);
							delaySpawn(236010, 6000);
							delaySpawn(236011, 9000);
							delaySpawn(236011, 9500);
							break;
						case 3:
							delaySpawn(236011, 1000);
							delaySpawn(236011, 1500);
							delaySpawn(236011, 6000);
							delaySpawn(236011, 6500);
							delaySpawn(236011, 7000);
							break;
						case 4:
							delaySpawn(236011, 1000);
							delaySpawn(236011, 1500);
							delaySpawn(236013, 2000);
							break;
						case 5:
							delaySpawn(236010, 2000);
							delaySpawn(236010, 5000);
							delaySpawn(236010, 7000);
							delaySpawn(236011, 11000);
							delaySpawn(236011, 11500);
							break;
						case 6:
							delaySpawn(236011, 1000);
							delaySpawn(236011, 1500);
							delaySpawn(236013, 2000);
							delaySpawn(236011, 6000);
							delaySpawn(236011, 6500);
							break;
						case 7:
							delaySpawn(236012, 7000);
							delaySpawn(236012, 7500);
							delaySpawn(236014, 8000);
							break;
					}
					break;
			}
		}, 0, delay);
	}

	/**
	 * Used for way C (Torment's Forge).
	 */
	private void delaySpawn(Player p, int npcId, int delay) {
		ThreadPoolManager.getInstance().schedule(() -> rndSpawnInRange(p, npcId, 5), delay);
	}

	private void rndSpawnInRange(Player player, int npcId, int distance) {
		double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
		float x = player.getX() + (float) (Math.cos(angleRadians) * distance);
		float y = player.getY() + (float) (Math.sin(angleRadians) * distance);
		Vector3f pos = GeoService.getInstance().getClosestCollision(player, x, y, player.getZ());
		byte headingTowardsPlayer = PositionUtil.getHeadingTowards(pos.getX(), pos.getY(), player.getX(), player.getY());
		spawn(npcId, pos.getX(), pos.getY(), pos.getZ(), headingTowardsPlayer);
	}

	@Override
	public void onCreatureDetected(Npc detector, Creature detected) {
		if (detected instanceof Player) {
			switch (detector.getNpcId()) {
				case 206197:
					detector.getController().delete();
					instance.setDoorState(219, true); // collapse pathway
					break;
				case 206198:
					if (spawnTask == null && isEventStarted.compareAndSet(false, true)) {
						detector.getController().delete();
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_B_START());
						delaySpawn((byte) 1, 10000);
					}
					break;
				case 206199:
					if (spawnTask == null && isEventStarted.compareAndSet(true, false)) {
						detector.getController().delete();
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_B_START());
						delaySpawn((byte) 2, 10000);
					}
					break;
			}
		}
	}

	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (zone.getZoneTemplate().getName() == ZoneName.get("IDRAKSHA_SOLO_WAVE_01_206399_1_300610000")) {
			if (spawnTask == null && isEventStarted.compareAndSet(false, true)) {
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_TAMES_SOLO_A_START());
				delaySpawn((byte) 3, 20000);
			}
		}
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
		if (spawnTask != null)
			spawnTask.cancel(true);
	}

}
