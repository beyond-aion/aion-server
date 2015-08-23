package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_START_IDABRE;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Ritsu Guide:
 *         http://power.plaync.co.kr/aion/%EC%82%AC%ED%8A%B8%EB%9D%BC%EC%9D%98+%EB%B9%84%EB%B0%80%EC%B0%BD%EA%B3%A0
 * @modified Whoop
 */
@InstanceID(300470000)
public class SatraTreasureHoardInstance extends GeneralInstanceHandler {

	private boolean rewarded = false;
	private Map<Integer, StaticDoor> doors;
	private AtomicBoolean isStartTimer = new AtomicBoolean();
	private Future<?> failTask;

	@Override
	public void onInstanceDestroy() {
		doors.clear();
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		doors = instance.getDoors();
		//doors.get(77).setOpen(true);
		spawnTimerRing();
	}

	private void spawnTimerRing() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("SATRAS_01", mapId, new Point3D(501.13412, 672.4659, 177.10771), new Point3D(492.13412,
			672.4659, 177.10771), new Point3D(496.54834, 671.5966, 184.10771), 8), instanceId);
		f1.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("SATRAS_01")) {
			if (isStartTimer.compareAndSet(false, true)) {
				for (Player p: instance.getPlayersInside()) {
 					PacketSendUtility.sendPacket(p, STR_MSG_INSTANCE_START_IDABRE);
 					PacketSendUtility.sendPacket(p, new SM_QUEST_ACTION(0, 600));
				}
				doors.get(77).setOpen(false);				
				startFail();				
			}
		}
		return false;
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 219301:
				int door = Rnd.get(1, 2);
				if (door == 1) {
					doors.get(84).setOpen(true);
					sendMsg(1401230);
				}
				else {
					doors.get(88).setOpen(true);
					sendMsg(1401229);
				}
				break;
			case 219299: // muzzled punisher
			case 219300: // punisher unleashed				
				if (failTask != null && !failTask.isCancelled())
					failTask.cancel(true);
				
				spawnChests(npc);				
				spawn(730588, 496.600f, 685.600f, 176.400f, (byte) 30); // Spawn Exit
				instance.doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player p) {
						if (p.isOnline())
							PacketSendUtility.sendPacket(p, new SM_QUEST_ACTION(0, 0));
					}
				});
				break;
			case 701464: // artifact spawn stronger boss
				Npc boss = getNpc(219299);
				if (boss != null && !boss.getLifeStats().isAlreadyDead()) {
					spawn(219300, boss.getX(), boss.getY(), boss.getZ(), boss.getHeading());
					boss.getController().onDelete();
				}
				break;
			case 219298:
				doors.get(62).setOpen(true);
				doors.get(108).setOpen(true);
				doors.get(118).setOpen(true);
				sendMsg(1401231);
				break;
			case 219297:
				doors.get(82).setOpen(true);
				doors.get(86).setOpen(true);
				doors.get(117).setOpen(true);
				sendMsg(1401231);
				break;
		}
	}

	private void spawnChests(Npc npc) {
		if (!rewarded) {		
			rewarded = true;
			int chest = npc.getNpcId() == 219299 ? 701463 : 701462;
			//Outer Chests
			spawn(701461, 466.246f, 716.57f, 176.398f, (byte) 0);
			spawn(701461, 528.156f, 715.66f, 176.398f, (byte) 60);
			spawn(701461, 469.17f, 701.632f, 176.398f, (byte) 11);
			spawn(701461, 524.292f, 701.063f, 176.398f, (byte) 50);
			spawn(701461, 515.439f, 691.87f, 176.398f, (byte) 45);
			spawn(701461, 478.623f, 692.772f, 176.398f, (byte) 15);
			//Inner Chests
			spawn(chest, 446.962f, 744.254f, 178.071f, (byte) 0, 206);
			spawn(chest, 459.856f, 759.960f, 178.071f, (byte) 0, 81);
			spawn(chest, 533.697f, 760.551f, 178.071f, (byte) 0, 80);
			spawn(chest, 477.382f, 770.049f, 178.071f, (byte) 0, 83);
			spawn(chest, 497.030f, 773.931f, 178.071f, (byte) 0, 85);
			spawn(chest, 516.508f, 770.646f, 178.071f, (byte) 0, 122);
		}
	}
	
	private void startFail() {
		failTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				Npc boss1 = getNpc(219299);
				Npc boss2 = getNpc(219300);
				if (boss1 != null) {
					boss1.getController().onDelete();
				}
				else if(boss2 != null) {
					boss2.getController().onDelete();
				}
			}
		}, 600 * 1000);
	}
}
