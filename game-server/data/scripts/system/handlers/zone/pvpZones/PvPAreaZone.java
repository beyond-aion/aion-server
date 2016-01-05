package zone.pvpZones;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.handler.ZoneNameAnnotation;

/**
 * @author MrPoke
 */
@ZoneNameAnnotation(value = "LC1_PVP_SUB_C_110010000 DC1_PVP_ZONE_120010000")
public class PvPAreaZone extends PvPZone {

	@Override
	protected void doTeleport(Player player, ZoneName zoneName) {
		if (zoneName == ZoneName.get("LC1_PVP_SUB_C_110010000"))
			TeleportService2.teleportTo(player, 110010000, 1, 1470.3f, 1343.5f, 563.7f);
		else if (zoneName == ZoneName.get("DC1_PVP_ZONE_120010000"))
			TeleportService2.teleportTo(player, 120010000, 1, 1005.1f, 1528.9f, 222.1f);
	}
}
