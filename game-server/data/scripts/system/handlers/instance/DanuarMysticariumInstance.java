package instance;

import java.util.Map;
import java.util.concurrent.Future;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * After activating the start device one of three game events will be chosen.
 * The first one is a key collecting challenge with a time limit about 5
 * minutes. After that all monster and chests will despawn. It happens in
 * the first room, the way to the next one is blocked. After time expires
 * a portal to the prisons will be spawned, where you can open the doors
 * with your keys to kill the NPC behind them and get some ap relics.
 * 
 * The second one seems to be a hide&seek with a time limit about 5
 * minutes, too. The goal of this is actually not clear. After time expires
 * you get access to the last room with a "Test Subject ..." NPC, who drops
 * five rare manastone bundles. There is also a chest containing relics
 * behind the test subject.
 * 
 * The third one is a tower defense game, where wave of balaur spawn and
 * try to kill something. It seems to be finished after 5 minutes too with
 * the commander wave. Maybe he drops the missing ap relics or rare
 * manastone bundles. (written by Estrayl)
 * 
 * @author Cheatkiller, Tibald
 */
@InstanceID(300480000)
public class DanuarMysticariumInstance extends GeneralInstanceHandler {

	private Map<Integer, StaticDoor> doors;
	private Future<?> collectingPhaseEndTask;

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		doors = instance.getDoors();
	}

	@Override
	public void onOpenDoor(int door) {
		switch (door) {
			case 3:
			case 101:
				spawn(230058, 213.9430f, 508.9750f, 153.2284f, (byte) 110);
				break;
			case 6:
				spawn(230057, 226.7380f, 529.7029f, 153.03912f, (byte) 100);
				break;
			case 7:
				spawn(230051, 242.3215f, 540.8343f, 152.59f, (byte) 93);
				break;
			case 8:
				spawn(230052, 262.3589f, 544.4155f, 150.5014f, (byte) 83);
				break;
			case 10:
				spawn(230053, 296.3592f, 547.9075f, 148.7211f, (byte) 83);
				break;
			case 11:
				spawn(230054, 317.7392f, 544.2635f, 148.7996f, (byte) 80);
				break;
			case 12:
				spawn(230056, 337.1739f, 531.7154f, 148.4716f, (byte) 73);
				break;
			case 13:
				spawn(230055, 346.2431f, 511.4081f, 148.1805f, (byte) 66);
				break;
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 831146:
				// Find Start pyramid
				scheduleCollectingEnd();
				// Open door
				// teleport player
				npc.getController().onDelete();
				break;
		}
	}

	@Override
	public void onInstanceDestroy() {
		if (collectingPhaseEndTask != null && !collectingPhaseEndTask.isCancelled())
			collectingPhaseEndTask.cancel(true);
		doors.clear();
	}

	private void scheduleCollectingEnd() {
		collectingPhaseEndTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				// Spawn Portal to prisoners and despawn mobs + chests
			}
		}, 1800000); // Find messages and duration
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}

	@Override
	public void onExitInstance(Player player) {
		// remove keys
		TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
	}
}
