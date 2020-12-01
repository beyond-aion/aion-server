package admincommands;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.model.templates.worldraid.WorldRaidLocation;
import com.aionemu.gameserver.services.WorldRaidService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Whoop, Sykra
 */
public class WorldRaid extends AdminCommand {

	public WorldRaid() {
		super("worldraid", "Starts/stops the Beritra Invasion event.");

		// @formatter:off
		setSyntaxInfo(
				"list - Shows all available world raid locations",
				"active - Shows all active world raid locations",
				"start <location_id> - Starts the world raid for the given location",
				"stop <location_id> - Stops the world raid for the given location"
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (!EventsConfig.ENABLE_WORLDRAID) {
			sendInfo(player, "World raid currently is disabled.");
			return;
		}
		if (params.length < 1) {
			sendInfo(player);
			return;
		}

		if ("list".equalsIgnoreCase(params[0])) {
			sendInfo(player, createLocationList(DataManager.WORLD_RAID_DATA.getLocations().values(), "World raid locations:"));
		} else if ("active".equalsIgnoreCase(params[0])) {
			sendInfo(player, createLocationList(WorldRaidService.getInstance().getActiveWorldRaidLocations(), "Currently active world raids:"));
		} else {
			if (params.length < 2 || !NumberUtils.isNumber(params[1])) {
				sendInfo(player);
				return;
			}

			int locationId = NumberUtils.toInt(params[1]);
			if (!WorldRaidService.getInstance().isValidWorldRaidLocation(locationId)) {
				sendInfo(player, "Invalid world raid location: " + locationId);
				return;
			}

			if ("start".equalsIgnoreCase(params[0])) {
				if (WorldRaidService.getInstance().isWorldRaidInProgress(locationId)) {
					sendInfo(player, "World raid for location " + locationId + " is already in progress");
					return;
				}
				sendInfo(player, "Starting world raid for location " + locationId);
				WorldRaidService.getInstance().startRaid(locationId, false);
			} else if ("stop".equalsIgnoreCase(params[0])) {
				if (!WorldRaidService.getInstance().isWorldRaidInProgress(locationId)) {
					sendInfo(player, "World raid for location " + locationId + " is not started.");
					return;
				}
				sendInfo(player, "Stopped world raid for location " + locationId);
				WorldRaidService.getInstance().stopRaid(locationId);
			} else {
				sendInfo(player);
			}
		}
	}

	private String createLocationList(final Collection<WorldRaidLocation> locations, final String header) {
		final StringBuilder sb = new StringBuilder();
		if (header != null && !header.isEmpty())
			sb.append(header);
		if (locations == null || locations.isEmpty()) {
			sb.append("\n\tNo locations available!");
			return sb.toString();
		}

		Map<String, List<WorldRaidLocation>> locationsByMapId = locations.stream().collect(Collectors.groupingBy(worldRaidLocation -> {
			WorldMapTemplate mapTemplate = DataManager.WORLD_MAPS_DATA.getTemplate(worldRaidLocation.getMapId());
			if (mapTemplate == null || mapTemplate.getName().isEmpty())
				return String.valueOf(worldRaidLocation.getMapId());
			return mapTemplate.getName();
		}, Collectors.toList()));

		locationsByMapId.keySet().stream().sorted().forEach(mapName -> {
			List<WorldRaidLocation> locationsForMap = locationsByMapId.get(mapName);
			if (locationsForMap == null)
				return;
			sb.append("\n\t").append(ChatUtil.color(mapName, Color.WHITE)).append(" - ");
			sb.append(locationsForMap.stream().map(this::createPositionString).collect(Collectors.joining(", ")));
		});
		return sb.toString();
	}

	private String createPositionString(final WorldRaidLocation location) {
		return ChatUtil.position(String.valueOf(location.getLocationId()), location.getMapId(), location.getX(), location.getY(), location.getZ());
	}

}
