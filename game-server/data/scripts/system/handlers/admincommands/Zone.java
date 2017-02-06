package admincommands;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author ATracer
 */
public class Zone extends AdminCommand {

	public Zone() {
		super("zone");
	}

	@Override
	public void execute(Player admin, String... params) {
		Creature target;
		if (admin.getTarget() == null || !(admin.getTarget() instanceof Creature))
			target = admin;
		else
			target = (Creature) admin.getTarget();
		if (params.length == 0) {
			List<ZoneInstance> zones = target.findZones();
			if (zones.isEmpty()) {
				PacketSendUtility.sendMessage(admin, target.getName() + " is not in any zone.");
			} else {
				PacketSendUtility.sendMessage(admin, target.getName() + "'s zone(s): ");
				if (admin.isInsideZoneType(ZoneType.DAMAGE))
					PacketSendUtility.sendMessage(admin, "DAMAGE");
				if (admin.isInsideZoneType(ZoneType.FLY))
					PacketSendUtility.sendMessage(admin, "FLY");
				if (admin.isInsideZoneType(ZoneType.PVP))
					PacketSendUtility.sendMessage(admin, "PVP");
				if (admin.isInsideZoneType(ZoneType.SIEGE))
					PacketSendUtility.sendMessage(admin, "CASTLE");
				if (admin.isInsideZoneType(ZoneType.WATER))
					PacketSendUtility.sendMessage(admin, "WATER");
				for (ZoneInstance zone : zones) {
					PacketSendUtility.sendMessage(admin, zone.getAreaTemplate().getZoneName().name());
					PacketSendUtility.sendMessage(admin, "Fly: " + zone.canFly() + "; Glide: " + zone.canGlide());
					PacketSendUtility.sendMessage(admin, "Ride: " + zone.canRide() + "; Fly-ride: " + zone.canFlyRide());
					PacketSendUtility.sendMessage(admin, "Kisk: " + zone.canPutKisk() + "; Recall: " + zone.canRecall());
					PacketSendUtility.sendMessage(admin,
						"Same race duels: " + zone.isSameRaceDuelsAllowed() + "; Other race duels: " + zone.isOtherRaceDuelsAllowed());
					PacketSendUtility.sendMessage(admin, "PvP: " + zone.isPvpAllowed());
					PacketSendUtility.sendMessage(admin, "canReturnBattle: " + zone.canReturnToBattle());
				}
			}
		} else if ("?".equalsIgnoreCase(params[0])) {
			info(admin, null);
		} else if ("refresh".equalsIgnoreCase(params[0])) {
			admin.revalidateZones();
		} else if ("inside".equalsIgnoreCase(params[0])) {
			try {
				ZoneName name = ZoneName.get(params[1]);
				PacketSendUtility.sendMessage(admin, "isInsideZone: " + admin.isInsideZone(name));
			} catch (Exception e) {
				PacketSendUtility.sendMessage(admin, "Zone name missing!");
				PacketSendUtility.sendMessage(admin, "Syntax: //zone inside <zone name> ");
			}
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //zone refresh | inside");
	}
}
