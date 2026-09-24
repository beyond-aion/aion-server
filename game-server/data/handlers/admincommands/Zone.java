package admincommands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author ATracer
 */
public class Zone extends AdminCommand {

	public Zone() {
		super("zone", "Shows zone information.", """
				 - Shows info about your target's current zone(s).
				<zone name> - Shows info about your target's current zone(s), filtered by the given zone name.
				refresh - Refreshes your zones.
				""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length > 1) {
			sendInfo(admin);
			return;
		}
		if (params.length == 1 && "refresh".equalsIgnoreCase(params[0])) {
			admin.revalidateZones();
			return;
		}
		Creature target = admin.getTarget() instanceof Creature creature ? creature : admin;
		String zoneNameParam = params.length == 0 ? null : params[0];
		List<ZoneInstance> zones = findZones(target, zoneNameParam);
		String zoneTypes = Arrays.stream(ZoneType.values()).filter(target::isInsideZoneType).map(ZoneType::name).collect(Collectors.joining(", "));
		if (!zoneTypes.isEmpty())
			sendInfo(admin, name(target) + "'s zone types: " + zoneTypes);
		if (zones.isEmpty()) {
			sendInfo(admin, name(target) + " is not in " + (zoneNameParam == null ? "any zone" : zoneNameParam) + '.');
		} else {
			sendInfo(admin, name(target) + "'s " + (zones.size() == 1 ? "zone" : "zones") + ':');
			for (ZoneInstance zone : zones) {
				sendInfo(admin, zone.getAreaTemplate().getZoneName().name());
				sendInfo(admin, "Fly: " + zone.canFly() + "; Glide: " + zone.canGlide());
				sendInfo(admin, "Ride: " + zone.canRide() + "; Fly-ride: " + zone.canFlyRide());
				sendInfo(admin, "Kisk: " + zone.canPutKisk() + "; Recall: " + zone.canRecall());
				sendInfo(admin, "Same race duels: " + zone.isSameRaceDuelsAllowed() + "; Other race duels: " + zone.isOtherRaceDuelsAllowed());
				sendInfo(admin, "PvP: " + zone.isPvpAllowed());
				sendInfo(admin, "canReturnBattle: " + zone.canReturnToBattle());
			}
		}
	}

	private List<ZoneInstance> findZones(Creature creature, String zoneNameFilter) {
		List<ZoneInstance> zones = creature.findZones();
		if (zoneNameFilter != null) {
			ZoneName zoneName = ZoneName.get(zoneNameFilter);
			if (zoneName == ZoneName.NONE)
				throw new IllegalArgumentException("Invalid zone name.");
			zones = zones.stream().filter(zone -> zone.getZoneTemplate().getName() == zoneName).toList();
		}
		return zones;
	}
}
