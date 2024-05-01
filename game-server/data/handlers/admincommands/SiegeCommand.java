package admincommands;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.siege.SiegeType;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.services.siege.BalaurAssaultService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

public class SiegeCommand extends AdminCommand {

	public SiegeCommand() {
		super("siege", "Controls sieges and artifacts.");

		// @formatter:off
		setSyntaxInfo(
			"locations - Shows info about all locations.",
			"start <locationId> - Starts the siege at the given location.",
			"stop <locationId> - Stops the siege at the given location.",
			"capture <locationId> [elyos|asmodians|balaur|legionName|legionId] - Captures the fortress at given location.",
			"assault <locationId> [delaySec] - Starts an assault at the given location."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		switch (params.length == 0 ? "" : params[0].toLowerCase()) {
			case "locations" -> listLocations(player);
			case "start" -> startSiege(player, parseLocation(params));
			case "stop" -> stopSiege(player, parseLocation(params));
			case "capture" -> capture(player, parseLocation(params), params);
			case "assault" -> assault(player, parseLocation(params), params.length < 3 ? 0 : Integer.parseInt(params[2]));
			default -> sendInfo(player);
		}
	}

	private void listLocations(Player player) {
		Collection<List<SiegeLocation>> locations = SiegeService.getInstance().getSiegeLocations().values().stream()
			.sorted(Comparator.comparingInt(SiegeLocation::getLocationId))
			.collect(Collectors.groupingBy(l -> (l.getLocationId() - 1) / 10, LinkedHashMap::new, Collectors.toList())).values();
		for (List<SiegeLocation> siegeLocations : locations) {
			for (int i = 0; i < siegeLocations.size(); i++) {
				SiegeLocation loc = siegeLocations.get(i);
				String worldName = DataManager.WORLD_MAPS_DATA.getTemplate(loc.getTemplate().getWorldId()).getName();
				String name = loc.getTemplate().getL10nId() == 0 ? loc.getType().toString() : loc.getTemplate().getL10n();
				String message = name + " (ID: " + loc.getLocationId() + ") in " + worldName + " belongs to " + loc.getRace();
				int secondsLeft = SiegeService.getInstance().getRemainingSiegeTimeInSeconds(loc.getLocationId());
				if (secondsLeft > 0)
					message += " (" + secondsLeft / 60 + "m " + secondsLeft % 60 + "s until siege ends)";
				if (i > 0 && loc.getType() == SiegeType.ARTIFACT)
					message = '\t' + message;
				sendInfo(player, message);
			}
		}
	}

	private void startSiege(Player player, SiegeLocation loc) {
		if (SiegeService.getInstance().isSiegeInProgress(loc.getLocationId())) {
			sendInfo(player, "This location is already under siege.");
		} else {
			SiegeService.getInstance().startSiege(loc.getLocationId());
			sendInfo(player, "Started siege at " + getLocationName(loc));
		}
	}

	private void stopSiege(Player player, SiegeLocation loc) {
		if (!SiegeService.getInstance().isSiegeInProgress(loc.getLocationId())) {
			sendInfo(player, "This location is not under siege.");
		} else {
			SiegeService.getInstance().stopSiege(loc.getLocationId());
			sendInfo(player, "Stopped siege at " + getLocationName(loc));
		}
	}

	private void capture(Player player, SiegeLocation loc, String[] params) {
		SiegeRace sr = null;
		Legion legion = null;
		if (params.length >= 3) {
			try {
				sr = SiegeRace.valueOf(params[2].toUpperCase());
			} catch (IllegalArgumentException ignored) {
				try {
					int legionId = Integer.parseInt(params[2]);
					legion = LegionService.getInstance().getLegion(legionId);
				} catch (NumberFormatException e) {
					String legionName = "";
					for (int i = 2; i < params.length; i++)
						legionName += " " + params[i];
					legion = LegionService.getInstance().getLegion(legionName.trim());
				}
				if (legion != null) {
					sr = SiegeRace.getByRace(PlayerService.getOrLoadPlayerCommonData(legion.getBrigadeGeneral()).getRace());
				}
			}
			if (legion == null && sr == null) {
				sendInfo(player, params[2] + " is not valid race or legion");
				return;
			}
		} else {
			sr = SiegeRace.getByRace(player.getRace());
		}
		SiegeService.getInstance().captureSiege(sr, legion != null ? legion.getLegionId() : 0, loc.getLocationId());
	}

	private void assault(Player player, SiegeLocation loc, int delaySeconds) {
		if (BalaurAssaultService.getInstance().startAssault(loc.getLocationId(), delaySeconds))
			sendInfo(player, "Started assault on " + getLocationName(loc));
		else {
			if (SiegeService.getInstance().isSiegeInProgress(loc.getLocationId()))
				sendInfo(player, "Assault on " + getLocationName(loc) + " was already started.");
			else
				sendInfo(player, getLocationName(loc) + " must be under siege in order to start an assault.");
		}
	}

	private SiegeLocation parseLocation(String[] params) {
		SiegeLocation location = params.length < 2 ? null : SiegeService.getInstance().getSiegeLocation(Integer.parseInt(params[1]));
		if (location == null)
			throw new IllegalArgumentException("Invalid locationId.");
		return location;
	}

	private static Object getLocationName(SiegeLocation loc) {
		return loc.getTemplate().getL10nId() == 0 ? loc.getType().toString() + " " + loc.getLocationId() : loc.getTemplate().getL10n();
	}
}
