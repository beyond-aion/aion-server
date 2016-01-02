package instance.abyss;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_START_IDABRE;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author keqi, xTz
 * @reworked Luzien
 */
@InstanceID(300120000)
public class KysisInstance extends GeneralInstanceHandler {

	private boolean rewarded = false;
	private AtomicBoolean isStartTimer = new AtomicBoolean();
	private Future<?> failTask;

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		spawnTimerRing();
		spawn(Rnd.get(215173, 215176), 527.64f, 212.0511f, 178.4134f, (byte) 90);
	}

	private void spawnTimerRing() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("KYSIS_01", mapId, new Point3D(530.15, 757.56, 199.42), new Point3D(526.52, 757.33, 199.42),
			new Point3D(528.156, 757.53, 205.10771), 8), instanceId);
		f1.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("KYSIS_01")) {
			if (isStartTimer.compareAndSet(false, true)) {
				for (Player p : instance.getPlayersInside()) {
					PacketSendUtility.sendPacket(p, STR_MSG_INSTANCE_START_IDABRE);
					PacketSendUtility.sendPacket(p, new SM_QUEST_ACTION(0, 600));
				}
				startFail();
			}
		}
		return false;
	}

	private void startFail() {
		failTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				Npc boss1 = getNpc(215179);
				Npc boss2 = getNpc(215178);
				if (boss1 != null) {
					boss1.getController().onDelete();
				} else if (boss2 != null) {
					boss2.getController().onDelete();
				}
			}
		}, 60000 * 10);
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 215146:
				openDoor(10);
				break;
			case 215169:
				openDoor(8);
				break;
			case 215170:
				openDoor(9);
				break;
			case 215145:
				openDoor(5);
				break;
			case 215148:
				openDoor(6);
				break;
			case 215171:
				openDoor(13);
				break;
			case 215147:
				openDoor(30);
				break;
			case 215172:
				openDoor(7);
				break;
			case 215159:
				openDoor(12);
				break;
			case 215177:
				openDoor(16);
				break;
			case 215160:
				openDoor(32);
				break;
			case 215158:
				openDoor(31);
				break;
			case 215157:
				openDoor(29);
				break;
			case 215179: // bosses
			case 215178:
				if (failTask != null && !failTask.isCancelled()) {
					failTask.cancel(true);
				}
				spawnChests(npc);
				break;
			case 215414: // artifact spawns weakened boss
				Npc boss = getNpc(215179);
				if (boss != null && !boss.getLifeStats().isAlreadyDead()) {
					spawn(215178, boss.getX(), boss.getY(), boss.getZ(), boss.getHeading());
					boss.getController().onDelete();
				}
		}
	}

	private void openDoor(int staticId) {
		for (Npc npc : instance.getNpcs()) {
			if (npc.getSpawn().getStaticId() == staticId)
				npc.getController().onDelete();
		}
	}

	private void spawnChests(Npc npc) {
		if (!rewarded) {
			rewarded = true; // safety mechanism
			if (npc.getNpcId() == 215222) {
				spawn(700560, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(700560, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(700560, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(700560, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(700560, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(700560, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(700560, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(700560, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(700560, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(700560, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(700560, 485.365f, 820.3497f, 199.45955f, (byte) 9);
				spawn(700542, 576.4634f, 837.3374f, 199.7f, (byte) 56); // Treasure Room Chest
			} else {
				spawn(700541, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(700541, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(700541, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(700541, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(700541, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(700541, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(700541, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(700541, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(700541, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(700541, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(700541, 485.365f, 820.3497f, 199.45955f, (byte) 9);
			}
		}
	}

	@Override
	public void onInstanceDestroy() {
		if (failTask != null && !failTask.isCancelled()) {
			failTask.cancel(true);
		}
	}
}
