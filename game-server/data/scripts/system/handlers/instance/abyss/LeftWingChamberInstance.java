package instance.abyss;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300080000)
public class LeftWingChamberInstance extends GeneralInstanceHandler {

	private AtomicLong startTime = new AtomicLong();
	private boolean isInstanceDestroyed = false;
	private Race instanceRace;

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		spawnRings();
		// spawn Treasurer Nabatma (pool=1)
		switch (Rnd.get(1, 2)) {
			case 1:
				spawn(215424, 502.1326f, 502.89673f, 352.94437f, (byte) 20);
				break;
			case 2:
				spawn(215424, 508.71814f, 660.93994f, 352.94638f, (byte) 60);
				break;
		}
	}

	private void spawnRings() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("LEFT_WING_1", mapId, new Point3D(576.2102, 585.4146, 353.90677), new Point3D(576.2102, 585.4146,
			359.90677), new Point3D(575.18384, 596.36664, 353.90677), 10), instanceId);
		f1.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("LEFT_WING_1")) {
			if (startTime.compareAndSet(0, System.currentTimeMillis())) {
				PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, 900));
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						despawnNpcs(getNpcs(700466));
						despawnNpcs(getNpcs(701481));
						despawnNpcs(getNpcs(701486));
					}
				}, 900000);
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

	private List<Npc> getNpcs(int npcId) {
		if (!isInstanceDestroyed) {
			return instance.getNpcs(npcId);
		}
		return null;
	}

	private void despawnNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			npc.getController().onDelete();
		}
	}

	private void spawnGoldChest() {
		final int chestId = instanceRace.equals(Race.ELYOS) ? 701481 : 701486;
		spawn(chestId, 496.87f, 664.07f, 352.94f, (byte) 90);
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
		startTime.set(0);
	}
}
