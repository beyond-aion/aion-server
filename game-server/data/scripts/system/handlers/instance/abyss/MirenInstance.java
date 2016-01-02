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
@InstanceID(300130000)
public class MirenInstance extends GeneralInstanceHandler {

	private boolean rewarded = false;
	private AtomicBoolean isStartTimer = new AtomicBoolean();
	private Future<?> failTask;

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		spawnTimerRing();
		spawn(Rnd.get(215216, 215219), 527.64f, 212.0511f, 178.4134f, (byte) 90);
	}

	private void spawnTimerRing() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("MIREN_01", mapId, new Point3D(530.15, 757.56, 199.42), new Point3D(526.52, 757.33, 199.42),
			new Point3D(528.156, 757.53, 205.10771), 8), instanceId);
		f1.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("MIREN_01")) {
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
				Npc boss1 = getNpc(215222);
				Npc boss2 = getNpc(215221);
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
			case 215191:
				openDoor(11);
				break;
			case 215188:
				openDoor(78);
				break;
			case 215202:
				openDoor(81);
				break;
			case 215203:
				openDoor(8);
				break;
			case 215215:
				openDoor(82);
				break;
			case 215212:
				openDoor(13);
				break;
			case 215189:
				openDoor(77);
				break;
			case 215214:
				openDoor(16);
				break;
			case 215200:
				openDoor(12);
				break;
			case 215220:
				openDoor(73);
				break;
			case 215213:
				openDoor(75);
				break;
			case 215201:
				openDoor(7);
				break;
			case 215190:
				openDoor(15);
				break;
			case 215222: // bosses
			case 215221:
				if (failTask != null && !failTask.isCancelled()) {
					failTask.cancel(true);
				}
				spawnChests(npc);
				break;
			case 215415: // artifact spawns weak boss
				Npc boss = getNpc(215222);
				if (boss != null && !boss.getLifeStats().isAlreadyDead()) {
					spawn(215221, boss.getX(), boss.getY(), boss.getZ(), boss.getHeading());
					boss.getController().onDelete();
				}
		}
	}

	private void openDoor(int staticId) {
		for (Npc npc : instance.getNpcs()) {
			if (npc.getSpawn().getStaticId() == staticId) {
				npc.getController().onDelete();
			}
		}
	}

	private void spawnChests(Npc npc) {
		if (!rewarded) {
			rewarded = true; // safety mechanism
			if (npc.getNpcId() == 215222) {
				spawn(700561, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(700561, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(700561, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(700561, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(700561, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(700561, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(700561, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(700561, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(700561, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(700561, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(700561, 485.365f, 820.3497f, 199.45955f, (byte) 9);
				spawn(700544, 576.8508f, 836.40424f, 199.3737f, (byte) 56); // Treasure Room Chest
			} else {
				spawn(700543, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(700543, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(700543, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(700543, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(700543, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(700543, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(700543, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(700543, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(700543, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(700543, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(700543, 485.365f, 820.3497f, 199.45955f, (byte) 9);
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
