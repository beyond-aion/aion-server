package instance.abyss;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_START_IDABRE;

import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author bobobear
 */
@InstanceID(300050000)
public class AsteriaInstance extends GeneralInstanceHandler {

	private AtomicLong startTime = new AtomicLong();
	private Race instanceRace;

	public AsteriaInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		new FlyRing(new FlyRingTemplate("ASTERIA_WING_1", mapId, new Point3D(479.24, 572.57, 202.72), new Point3D(477.95, 567.64, 212.9),
			new Point3D(477.97, 563.35, 202.12), 10), instance.getInstanceId()).spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("ASTERIA_WING_1")) {
			if (startTime.compareAndSet(0, System.currentTimeMillis())) {
				PacketSendUtility.sendPacket(player, STR_MSG_INSTANCE_START_IDABRE());
				PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, 900));
				ThreadPoolManager.getInstance().schedule(() -> deleteAliveNpcs(700475, 700476, 700477, 701483, 701488), 900000);
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
		spawn(instanceRace == Race.ELYOS ? 701483 : 701488, 512.8f, 565.35f, 198f, (byte) 60);
	}
}
