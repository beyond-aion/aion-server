package instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * After activating the start device one of three game events will be chosen.
 * The first one is a key collecting challenge with a time limit about 5
 * minutes. After that all monster and chests will despawn. It happens in
 * the first room, the way to the next one is blocked. After time expires
 * a portal to the prisons will be spawned, where you can open the doors
 * with your keys to kill the NPC behind them and get some ap relics.
 * The second one seems to be a hide&seek with a time limit about 5
 * minutes, too. The goal of this is actually not clear. After time expires
 * you get access to the last room with a "Test Subject ..." NPC, who drops
 * five rare manastone bundles. There is also a chest containing relics
 * behind the test subject.
 * The third one is a tower defense game, where wave of balaur spawn and
 * try to kill something. It seems to be finished after 5 minutes too with
 * the commander wave. Maybe he drops the missing ap relics or rare
 * manastone bundles. (written by Estrayl)
 * 
 * @author Yeats
 *         TODO: Mini Game 2 & 3
 */
@InstanceID(300480000)
public class DanuarMysticariumInstance extends GeneralInstanceHandler {

	private List<Future<?>> tasks;

	public DanuarMysticariumInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onOpenDoor(int door) {
		switch (door) {
			case 6 -> spawn(219964, 225.53f, 529.7f, 153.04f, (byte) 100);
			case 7 -> spawn(219963, 241.602f, 541.79f, 152.591f, (byte) 95);
			case 8 -> spawn(219965, 262.17f, 545.68f, 150.51f, (byte) 85);
			case 10 -> spawn(219964, 295.04f, 547.48f, 148.73f, (byte) 90);
			case 11 -> spawn(219963, 317.654f, 545.801f, 148.8f, (byte) 80);
			case 12 -> spawn(219965, 336.94f, 532.89f, 148.472f, (byte) 75);
			case 13 -> spawn(219969, 348.14f, 512.56f, 148.19f, (byte) 65);
			case 101 -> spawn(219963, 212.068f, 510.02f, 153.23f, (byte) 115);
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 731583:
				startTasks();
				instance.setDoorState(3, true);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5Re_solo_game1_1());
				TeleportService.teleportTo(player, instance, 140.45f, 182.2f, 242f, (byte) 10, TeleportAnimation.FADE_OUT_BEAM);
				npc.getController().delete();
				break;
			case 702715:
				TeleportService.teleportTo(player, instance, 236.1f, 488.86f, 152f, (byte) 25, TeleportAnimation.FADE_OUT_BEAM);
				break;
			case 702717:
				TeleportService.moveToInstanceExit(player, mapId, player.getRace());
				break;
		}
	}

	private void startTasks() {
		tasks = new ArrayList<>();
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5Re_solo_game1_2()), 125000));
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5Re_solo_game1_3()), 155000));
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5Re_solo_game1_4()), 175000));
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			deleteAliveNpcs(219958, 219959, 702700, 702701);
			spawn(702715, 169.366f, 208.93f, 188.02f, (byte) 0);
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5Re_solo_game1_5());
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5Re_solo_game1_6());
		}, 185000)); // 3min 5sec
	}

	private void cancelTasks() {
		if (tasks != null) {
			tasks.stream().filter(future -> future != null && !future.isCancelled()).forEach(future -> future.cancel(true));
		}
	}

	@Override
	public void onInstanceDestroy() {
		cancelTasks();
	}

	@Override
	public void leaveInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}
}
