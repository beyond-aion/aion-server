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
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Everlight for Aion-Lightning
 */
@InstanceID(301260000)
public class KrotanBattleInstance_L extends GeneralInstanceHandler {

	protected boolean rewarded = false;
	private AtomicBoolean isStartTimer = new AtomicBoolean();
	private Future<?> failTask;

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		spawn(Rnd.get(233627, 233629), 527.64f, 212.0511f, 178.4134f, (byte) 90);
		spawnTimerRing();
	}

	private void spawnTimerRing() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("KROTAN_02", mapId, new Point3D(530.15, 757.56, 199.42), new Point3D(526.52,
				757.33, 199.42), new Point3D(528.156, 757.53, 205.10771), 8), instanceId);
		f1.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("KROTAN_02")) {
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
				Npc boss1 = getNpc(233633);
				Npc boss2 = getNpc(233632);
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
			case 233599:
				openDoor(10);
				break;
			case 233600:
				openDoor(5);
				break;
			case 233626:
				openDoor(9);
				break;
			case 233602:
				openDoor(6);
				break;
			case 233623:
				openDoor(13);
				break;
			case 233601:
				openDoor(30);
				break;
			case 233625:
				openDoor(7);
				break;
			case 233613:
				openDoor(12);
				break;
			case 233631:
				openDoor(16);
				break;
			case 233611:
				openDoor(32);
				break;
			case 233612:
				openDoor(31);
				break;
			case 233624:
				openDoor(8);
				break;
			case 233614:
				openDoor(29);
				break;
			case 233633: // Enraged Krotan Lord
			case 233632: // Weakened Krotan Lord
				if (failTask != null && !failTask.isCancelled()) {
					failTask.cancel(true);
				}
				spawnChests(npc);
				break;

		}
		artifactSpawns(npc);
		statueKills(npc);
	}

	private void openDoor(int staticId) {
		for (Npc npc : instance.getNpcs()) {
			if (npc.getSpawn().getStaticId() == staticId) {
				npc.getController().onDelete();
			}
		}
	}

	private void statueKills(Npc npc) {
		int points = 0;
		switch (npc.getNpcId()) {
			case 235536:
				points = 3000;
				break;
			case 235537:
				points = 6000;
				break;
			case 235538:
				points = 12000;
				break;
		}
		if (points != 0) {
			for (Player p : instance.getPlayersInside()) {
				AbyssPointsService.addAp(p, points / instance.playersCount());
			}
		}
	}

	protected void artifactSpawns(Npc npc) {
		switch (npc.getNpcId()) {
			case 215413: //artifact spawns weak boss
				Npc boss = getNpc(233633);
				if (boss != null && !boss.getLifeStats().isAlreadyDead()) {
					spawn(233632, boss.getX(), boss.getY(), boss.getZ(), boss.getHeading());
					boss.getController().onDelete();
				}
				break;
		}
	}

	protected void spawnChests(Npc npc) {
		if (!rewarded) {
			rewarded = true; //safety mechanism
			if (npc.getNpcId() == 233632) {
				spawn(702290, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(702290, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(702290, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(702290, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(702290, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(702290, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(702290, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(702290, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(702290, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(702290, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(702290, 485.365f, 820.3497f, 199.45955f, (byte) 9);
				spawn(702291, 576.4634f, 837.3374f, 199.7f, (byte) 56); // Treasure Room Chest
			} else {
				spawn(702289, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(702289, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(702289, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(702289, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(702289, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(702289, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(702289, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(702289, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(702289, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(702289, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(702289, 485.365f, 820.3497f, 199.45955f, (byte) 9);
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
