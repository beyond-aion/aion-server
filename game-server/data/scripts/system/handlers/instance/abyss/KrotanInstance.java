package instance.abyss;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_START_IDABRE;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.network.util.ThreadPoolManager;
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
@InstanceID(300140000)
public class KrotanInstance extends GeneralInstanceHandler {

	private boolean rewarded = false;
	private AtomicBoolean isStartTimer = new AtomicBoolean();
	private Future<?> failTask;

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		spawnTimerRing();
		spawn(Rnd.get(215130, 215133), 527.64f, 212.0511f, 178.4134f, (byte) 90);
	}

	private void spawnTimerRing() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("KROTAN_01", mapId, new Point3D(530.15, 757.56, 199.42), new Point3D(526.52, 757.33, 199.42),
			new Point3D(528.156, 757.53, 205.10771), 8), instanceId);
		f1.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("KROTAN_01")) {
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
				Npc boss1 = getNpc(215135);
				Npc boss2 = getNpc(215136);
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
			case 215102:
				openDoor(10);
				break;
			case 215103:
				openDoor(5);
				break;
			case 215105:
				openDoor(6);
				break;
			case 215104:
				openDoor(30);
				break;
			case 215126:
				openDoor(13);
				break;
			case 215128:
				openDoor(7);
				break;
			case 215116:
				openDoor(12);
				break;
			case 215134:
				openDoor(16);
				break;
			case 215114:
				openDoor(32);
				break;
			case 215115:
				openDoor(31);
				break;
			case 215129:
				openDoor(9);
				break;
			case 215127:
				openDoor(8);
				break;
			case 215117:
				openDoor(29);
				break;
			case 215136: // bosses
			case 215135:
				if (failTask != null && !failTask.isCancelled()) {
					failTask.cancel(true);
				}
				spawnChests(npc);
				break;
			case 215413: // artifact spawns weak boss
				Npc boss = getNpc(215136);
				if (boss != null && !boss.getLifeStats().isAlreadyDead()) {
					spawn(215135, boss.getX(), boss.getY(), boss.getZ(), boss.getHeading());
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
				spawn(700559, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(700559, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(700559, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(700559, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(700559, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(700559, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(700559, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(700559, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(700559, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(700559, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(700559, 485.365f, 820.3497f, 199.45955f, (byte) 9);
				spawn(700540, 576.4634f, 837.3374f, 199.7f, (byte) 56); // Treasure Room Chest
			} else {
				spawn(700539, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(700539, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(700539, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(700539, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(700539, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(700539, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(700539, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(700539, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(700539, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(700539, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(700539, 485.365f, 820.3497f, 199.45955f, (byte) 9);
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
