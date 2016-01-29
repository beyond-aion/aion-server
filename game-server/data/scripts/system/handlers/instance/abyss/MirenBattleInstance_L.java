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
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Everlight for Aion-Lightning
 */
@InstanceID(301250000)
public class MirenBattleInstance_L extends GeneralInstanceHandler {

	protected boolean rewarded = false;
	private AtomicBoolean isStartTimer = new AtomicBoolean();
	private Future<?> failTask;

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		spawn(Rnd.get(233713, 233716), 527.64f, 212.0511f, 178.4134f, (byte) 90);
		spawnTimerRing();
	}

	private void spawnTimerRing() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("MIREN_02", mapId, new Point3D(530.15, 757.56, 199.42), new Point3D(526.52, 757.33, 199.42),
			new Point3D(528.156, 757.53, 205.10771), 8), instanceId);
		f1.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("MIREN_02")) {
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
				Npc boss1 = getNpc(233719);
				Npc boss2 = getNpc(233718);
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
			case 233688:
				openDoor(11);
				break;
			case 233700:
				openDoor(8);
				break;
			case 233712:
				openDoor(82);
				break;
			case 233709:
				openDoor(13);
				break;
			case 233686:
				openDoor(77);
				break;
			case 233711:
				openDoor(16);
				break;
			case 233697:
				openDoor(12);
				break;
			case 233717:
				openDoor(73);
				break;
			case 233710:
				openDoor(75);
				break;
			case 233699:
				openDoor(81);
				break;
			case 233685:
				openDoor(78);
				break;
			case 233687:
				openDoor(15);
				break;
			case 233698:
				openDoor(7);
				break;
			case 233719: // Miren Duke lvl.65
			case 233718: // weakened boss lvl.65
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
			case 215415: // artifact spawns weak boss
				Npc boss = getNpc(233719);
				if (boss != null && !boss.getLifeStats().isAlreadyDead()) {
					spawn(233718, boss.getX(), boss.getY(), boss.getZ(), boss.getHeading());
					boss.getController().onDelete();
				}
				break;
		}
	}

	protected void spawnChests(Npc npc) {
		if (!rewarded) {
			rewarded = true; // safety mechanism
			if (npc.getNpcId() == 233719) {
				spawn(702298, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(702298, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(702298, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(702298, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(702298, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(702298, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(702298, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(702298, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(702298, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(702298, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(702298, 485.365f, 820.3497f, 199.45955f, (byte) 9);
				spawn(702299, 576.4634f, 837.3374f, 199.7f, (byte) 56); // Treasure Room Chest
			} else {
				spawn(702297, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
				spawn(702297, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
				spawn(702297, 560.082f, 882.97943f, 199.37367f, (byte) 76);
				spawn(702297, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
				spawn(702297, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
				spawn(702297, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
				spawn(702297, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
				spawn(702297, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
				spawn(702297, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
				spawn(702297, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
				spawn(702297, 485.365f, 820.3497f, 199.45955f, (byte) 9);
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
