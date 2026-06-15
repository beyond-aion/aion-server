package ai.portals;

import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;

import ai.ActionItemNpcAI;

/**
 * @author Estrayl
 */
@AIName("dramata_portal")
public class DramataPortalAI extends ActionItemNpcAI {

	private WorldPosition targetLocation;

	public DramataPortalAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		switch (getPosition().getMapId()) {
			case 110070000:
				PacketSendUtility.broadcastToWorld(new SM_MESSAGE(getOwner(),
					"A dangerous rift is materializing inside [pos:Kaisinel's Academy;0 110070000 485.69 407.96 0.0 0]. Use it and destroy the source of this anomaly!",
					ChatType.BRIGHT_YELLOW_CENTER), p -> p.getLevel() >= 65 && p.getRace() == Race.ELYOS);
				targetLocation = new WorldPosition(220070000, 2928.98f, 919.95f, 35.289f, (byte) 94);
				break;
			case 120080000:
				PacketSendUtility.broadcastToWorld(new SM_MESSAGE(getOwner(),
					"A dangerous rift is materializing inside [pos:Marchutan Priory;1 120080000 392.58 231.57 0.0 0]. Use it and destroy the source of this anomaly!",
					ChatType.BRIGHT_YELLOW_CENTER), p -> p.getLevel() >= 65 && p.getRace() == Race.ASMODIANS);
				targetLocation = new WorldPosition(210050000, 141.7f, 2066.3f, 438.66f, (byte) 34);
				break;
		}
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (targetLocation == null)
			return;
		if (player.getLevel() < 60) {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_CANNOT_USE_DIRECT_PORTAL_LEVEL_LIMIT_COMMON(getOwner().getObjectTemplate().getL10n()));
			return;
		}
		long timeDelta = getOwner().getMillisSinceSpawn();
		if (timeDelta < TimeUnit.MINUTES.toMillis(10)) {
			long approxMin = 10 - timeDelta / 1000 / 60;
			PacketSendUtility.sendMessage(player,
				"Wait Daeva! The rift is not stable enough to enter. We estimate it is safe to use in about " + approxMin + " minutes.");
			return;
		}
		TeleportService.teleportTo(player, targetLocation);
		super.handleUseItemFinish(player);
	}
}
