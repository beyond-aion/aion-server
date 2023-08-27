package instance.abyss;

import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300080000)
public class LeftWingChamberInstance extends GeneralInstanceHandler {

	private final AtomicLong startTime = new AtomicLong();
	private Race instanceRace;

	public LeftWingChamberInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		spawnRings();
		if (Rnd.nextBoolean()) // spawn Treasurer Nabatma (pool=1)
			spawn(215424, 502.1326f, 502.89673f, 352.94437f, (byte) 20);
		else
			spawn(215424, 508.71814f, 660.93994f, 352.94638f, (byte) 60);
	}

	private void spawnRings() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("LEFT_WING_1", mapId, new Point3D(576.2102, 585.4146, 353.90677),
			new Point3D(576.2102, 585.4146, 359.90677), new Point3D(575.18384, 596.36664, 353.90677), 10), instance.getInstanceId());
		f1.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("LEFT_WING_1")) {
			if (startTime.compareAndSet(0, System.currentTimeMillis())) {
				PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, 900));
				ThreadPoolManager.getInstance().schedule(() -> deleteAliveNpcs(700466, 701481, 701486), 900000);
			}
		}
		return false;
	}

	@Override
	public void onEnterInstance(Player player) {
		long start = startTime.get();
		if (start > 0) {
			long time = System.currentTimeMillis() - start;
			if (time < 900000) {
				PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, 900 - (int) time / 1000));
			}
		}

		if (instanceRace == null) {
			instanceRace = player.getRace();
			spawnGoldChest();
		}

	}

	private void spawnGoldChest() {
		spawn(instanceRace == Race.ELYOS ? 701481 : 701486, 496.87f, 664.07f, 352.94f, (byte) 90);
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		if (npc.getNpcId() == 700455)
			TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}
}
