package instance;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Sykra
 */
@InstanceID(320080000)
public class DraupnirCaveInstance extends GeneralInstanceHandler {

	private final AtomicBoolean isInstanceStartMessageSend = new AtomicBoolean();
	private int adjutantsKilled;

	public DraupnirCaveInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		new FlyRing(new FlyRingTemplate("INSTANCE_MSG_START", mapId, new Point3D(475.8222f, 427.8852f, 618.3719f),
			new Point3D(478.3698f, 450.1829f, 624.6974f), new Point3D(473.2742f, 405.5875f, 612.0464f), 20), instance.getInstanceId()).spawn();

		switch (Rnd.get(1, 4)) {
			case 1 -> spawn(213587, 567.438f, 700.875f, 538.701f, (byte) 7); // Hungry Ooze
			case 2 -> spawn(213588, 166.8f, 536.285f, 505.802f, (byte) 9); // Lucky Golden Saam
			case 3 -> spawn(213771, 497.006f, 434.713f, 616.584f, (byte) 71); // Protector Rakkan
			case 4 -> spawn(213773, 380.694f, 611.956f, 598.523f, (byte) 98); // Dragonpriest Tairgus
		}
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if ("INSTANCE_MSG_START".equals(flyingRing) && isInstanceStartMessageSend.compareAndSet(false, true)) {
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_BOSS_SPAWN_IDDF3_DRAGON_1());
			return true;
		}
		return false;
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 213776: // Instructor Afrane
			case 213778: // Beautiful Lakshmi
			case 213779: // Commander Nimbarka
			case 213802: // Kind Saraswati
				switch (++adjutantsKilled) {
					case 1 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_BOSS_SPAWN_IDDF3_DRAGON_2());
					case 2 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_BOSS_SPAWN_IDDF3_DRAGON_3());
					case 3 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_BOSS_SPAWN_IDDF3_DRAGON_4());
					case 4 -> {
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_BOSS_SPAWN_IDDF3_DRAGON());
						spawn(213780, 813.189f, 432.255f, 318.928f, (byte) 30); // Commander Bakarma
					}
				}
				break;
		}
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, true, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		TeleportService.teleportTo(player, instance, 492.83383f, 375.46542f, 622.26920f, (byte) 29);
		return true;
	}
}
