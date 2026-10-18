package admincommands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.zone.ZoneAttributes;
import com.aionemu.gameserver.world.zone.ZoneInstance;

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
		String zoneNameParam = params.length == 0 ? "" : params[0].toUpperCase();
		List<ZoneInstance> zones = findZones(target, zoneNameParam);
		String zoneTypes = Arrays.stream(ZoneType.values()).filter(target::isInsideZoneType).map(ZoneType::name).collect(Collectors.joining(", "));
		if (!zoneTypes.isEmpty())
			sendInfo(admin, name(target) + "'s zone types: " + zoneTypes);
		if (zones.isEmpty()) {
			sendInfo(admin, name(target) + " is not in " + (zoneNameParam.isEmpty() ? "any zone" : zoneNameParam + "*") + '.');
		} else {
			sendInfo(admin, name(target) + "'s " + (zones.size() == 1 ? "zone" : "zones") + ':');
			for (ZoneInstance zone : zones) {
				ZoneTemplate zt = zone.getZoneTemplate();
				sendInfo(admin, zt.getName());
				sendInfo(admin, "Fly: " + zt.hasZoneAttribute(ZoneAttributes.FLY) + "; Glide: " + zt.hasZoneAttribute(ZoneAttributes.GLIDE));
				sendInfo(admin, "Ride: " + zt.hasZoneAttribute(ZoneAttributes.RIDE) + "; Fly-ride: " + zt.hasZoneAttribute(ZoneAttributes.FLY_RIDE));
				sendInfo(admin, "Kisk: " + zt.hasZoneAttribute(ZoneAttributes.BIND) + "; Recall: " + zt.hasZoneAttribute(ZoneAttributes.RECALL));
				sendInfo(admin, "Same race duels: " + zt.hasZoneAttribute(ZoneAttributes.DUEL_SAME_RACE_ENABLED) + "; Other race duels: " + zt.hasZoneAttribute(ZoneAttributes.DUEL_OTHER_RACE_ENABLED));
				sendInfo(admin, "PvP: " + zt.hasZoneAttribute(ZoneAttributes.PVP_ENABLED));
				sendInfo(admin, "canReturnBattle: " + !zt.hasZoneAttribute(ZoneAttributes.NO_RETURN_BATTLE));
			}
		}
	}

	private List<ZoneInstance> findZones(Creature creature, String zoneNameUpperCase) {
		List<ZoneInstance> zones = creature.findZones();
		if (!zoneNameUpperCase.isEmpty())
			zones = zones.stream().filter(zone -> zone.getZoneTemplate().getName().toUpperCase().startsWith(zoneNameUpperCase)).toList();
		return zones;
	}
}
