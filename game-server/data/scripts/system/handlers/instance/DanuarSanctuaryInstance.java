package instance;

import java.util.Map;
import java.util.concurrent.Future;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu, Tibald
 */
@InstanceID(301140000)
public class DanuarSanctuaryInstance extends GeneralInstanceHandler {

	private Map<Integer, StaticDoor> doors;
	private Future<?> timer4minTask; // sysmsg
	private Future<?> timer8minTask; // sysmsg + bosses spawn
	private Future<?> timer12minTask; // sysmsg
	private Future<?> timer15minTask; // sysmsg + bosses despawn
	private Future<?> timer17minTask; // sysmsg
	private Future<?> timer20minTask; // sysmsg + keymasters despawn

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		doors = instance.getDoors();
	}

	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (zone.getAreaTemplate().getZoneName() == ZoneName.get("THE_CRYPTS_1_301140000")
			|| zone.getAreaTemplate().getZoneName() == ZoneName.get("THE_CHARNELS_1_301140000")
			|| zone.getAreaTemplate().getZoneName() == ZoneName.get("THE_CATACOMBS_1_301140000")) {
			if (timer4minTask == null) {
				sendMsg(1401855); // The Beritran Special Research Team commanders are nearing The Chamber of Ruin.
				timer4minTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						sendMsg(1401856); // The Beritran Special Research Team commanders have discovered The Chamber of Ruin.
					}

				}, 240000); // 4 min
			}
			if (timer8minTask == null) {
				timer8minTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						spawn(233089, 1056.7562f, 676.1657f, 282.385f, (byte) 30);
						spawn(233090, 1049.8118f, 689.37427f, 282.12994f, (byte) 105);
						spawn(233086, 1063.6968f, 689.211f, 282.12994f, (byte) 75);
						sendMsg(1401857); // The Beritran Special Research Team commanders have entered The Chamber of Ruin.
					}

				}, 480000); // 8 min
			}
			if (timer12minTask == null) {
				timer12minTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						sendMsg(1401858); // The Beritran Special Research Team commanders are collecting Danuar relics.
					}

				}, 720000); // 12 min
			}
			if (timer15minTask == null) {
				timer15minTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						deleteNpc(233089);
						deleteNpc(233090);
						deleteNpc(233086);
						sendMsg(1401859); // The Beritran Special Research Team commanders have departed with their treasures.
					}

				}, 900000); // 15 min
			}

			if (timer17minTask == null) {
				timer17minTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						sendMsg(1401860); // The Chir Grave Robbers are almost finished digging.
					}

				}, 1020000); // 17 min
			}
			if (timer20minTask == null) {
				timer20minTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						deleteNpc(233091);
						deleteNpc(233092);
						deleteNpc(233093);
						sendMsg(1401861); // The Chir Grave Robbers have left.
					}

				}, 1200000); // 20 min
			}
		}
		if (zone.getAreaTemplate().getZoneName() == ZoneName.get("THE_CHAMBER_OF_RUIN_1_301140000")) {
			cancelTasks();
		}
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 233187:
				spawn(233087, 906.4945f, 861.5854f, 280.5441f, (byte) 73, 1699);
				break;
			case 233391:
				sendMsg(1401946);
				break;
			case 233089:
			case 233090:
			case 233086:
				if (isDeadNpc(233089) && isDeadNpc(233090) && isDeadNpc(233086))
					spawn(701876, 1072.3127f, 683.08319f, 282.0391f, (byte) 60);
				break;
		}
	}

	private boolean isDeadNpc(int npcId) {
		return (getNpc(npcId) == null || getNpc(npcId).getLifeStats().isAlreadyDead());
	}

	@Override
	public void handleUseItemFinish(Player player, final Npc npc) {
		switch (npc.getNpcId()) {
			case 701859:
				openDoor(159);
				sendMsg(1401839);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), 10, 0));
				break;
			case 701860:
				openDoor(690);
				sendMsg(1401839);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), 10, 0));
				break;
			case 701861:
				openDoor(350);
				sendMsg(1401839);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), 10, 0));
				break;
			case 701862:
				openDoor(160);
				sendMsg(1401839);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), 10, 0));
				break;
			case 701863:
				openDoor(10);
				sendMsg(1401839);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), 10, 0));
				break;
			case 701864:
				openDoor(154);
				sendMsg(1401839);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), 10, 0));
				break;
			case 730864:
				for (Npc door : instance.getNpcs(233142)) {
					if (door.getSpawn().getStaticId() == 7) {
						door.getController().onDelete();
						return;
					}
				}
				sendMsg(1401937);
				break;
			case 730863:
				for (Npc door : instance.getNpcs(233142)) {
					if (door.getSpawn().getStaticId() == 1820) {
						door.getController().onDelete();
						return;
					}
				}
				sendMsg(1401937);
				break;
		}
	}

	private void openDoor(int doorId) {
		StaticDoor door = doors.get(doorId);
		if (door != null)
			door.setOpen(true);
	}

	private void deleteNpc(int npcId) {
		if (getNpc(npcId) != null && !getNpc(npcId).getLifeStats().isAlreadyDead()) {
			getNpc(npcId).getController().onDelete();
		}
	}

	private void cancelTasks() {
		if (timer4minTask != null && !timer4minTask.isCancelled()) {
			timer4minTask.cancel(true);
		}
		if (timer8minTask != null && !timer8minTask.isCancelled()) {
			timer8minTask.cancel(true);
		}
		if (timer12minTask != null && !timer12minTask.isCancelled()) {
			timer12minTask.cancel(true);
		}
		if (timer15minTask != null && !timer15minTask.isCancelled()) {
			timer15minTask.cancel(true);
		}
		if (timer17minTask != null && !timer17minTask.isCancelled()) {
			timer17minTask.cancel(true);
		}
		if (timer20minTask != null && !timer20minTask.isCancelled()) {
			timer20minTask.cancel(true);
		}
	}

	@Override
	public void onInstanceDestroy() {
		doors.clear();
		cancelTasks();
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}

}
