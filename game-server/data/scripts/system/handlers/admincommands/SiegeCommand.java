package admincommands;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.OutpostLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.services.siege.BalaurAssaultService;
import com.aionemu.gameserver.services.siege.Siege;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

public class SiegeCommand extends AdminCommand {

	public SiegeCommand() {
		super("siege", "Controls sieges and artifacts");

		// @formatter:off
		setSyntaxInfo(
			"<start|stop> <locationId> - Starts/stops the siege at the given location.",
			"<list> <locations> - Shows all fortress, outpost and artifact locations.",
			"<list> <sieges> - Shows all currently active sieges.",
			"<capture> <locationId> <ELYOS|ASMODIANS|BALAUR|legionName|legionId> - Captures the fortress at given location as the specified owner.",
			"<assault> <locationId> <delaySec> - Starts an assault at the given location with the specified delay in seconds."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			showHelp(player);
			return;
		}

		if ("stop".equalsIgnoreCase(params[0]) || "start".equalsIgnoreCase(params[0])) {
			handleStartStopSiege(player, params);
		} else if ("list".equalsIgnoreCase(params[0])) {
			handleList(player, params);
		} else if ("capture".equals(params[0])) {
			capture(player, params);
		} else if ("assault".equals(params[0])) {
			assault(player, params);
		}
	}

	private void handleStartStopSiege(Player player, String... params) {
		if (params.length != 2 || !NumberUtils.isDigits(params[1])) {
			showHelp(player);
			return;
		}

		int siegeLocId = NumberUtils.toInt(params[1]);
		if (!isValidSiegeLocationId(player, siegeLocId)) {
			showHelp(player);
			return;
		}

		if ("start".equalsIgnoreCase(params[0])) {
			if (SiegeService.getInstance().isSiegeInProgress(siegeLocId)) {
				sendInfo(player, "Siege Location " + siegeLocId + " is already under siege");
			} else {
				sendInfo(player, "Siege Location " + siegeLocId + " - starting siege!");
				SiegeService.getInstance().startSiege(siegeLocId);
			}
		} else if ("stop".equalsIgnoreCase(params[0])) {
			if (!SiegeService.getInstance().isSiegeInProgress(siegeLocId)) {
				sendInfo(player, "Siege Location " + siegeLocId + " is not under siege");
			} else {
				sendInfo(player, "Siege Location " + siegeLocId + " - stopping siege!");
				SiegeService.getInstance().stopSiege(siegeLocId);
			}
		}
	}

	private boolean isValidSiegeLocationId(Player player, int fortressId) {

		if (!SiegeService.getInstance().getSiegeLocations().keySet().contains(fortressId)) {
			sendInfo(player, "Id " + fortressId + " is invalid");
			return false;
		}

		return true;
	}

	private void handleList(Player player, String[] params) {
		if (params.length != 2) {
			showHelp(player);
			return;
		}

		if ("locations".equalsIgnoreCase(params[1])) {
			listLocations(player);
		} else if ("sieges".equalsIgnoreCase(params[1])) {
			listSieges(player);
		} else {
			showHelp(player);
		}
	}

	private void listLocations(Player player) {
		for (FortressLocation f : SiegeService.getInstance().getFortresses().values()) {
			sendInfo(player, "Fortress: " + f.getLocationId() + " belongs to " + f.getRace());
		}
		for (OutpostLocation o : SiegeService.getInstance().getOutposts().values()) {
			sendInfo(player, "Outpost: " + o.getLocationId() + " belongs to " + o.getRace());
		}
		for (ArtifactLocation a : SiegeService.getInstance().getStandaloneArtifacts()) {
			sendInfo(player, "Artifact: " + a.getLocationId() + " belongs to " + a.getRace());
		}
	}

	private void listSieges(Player player) {
		StringBuilder sb = new StringBuilder();
		for (Integer i : SiegeService.getInstance().getSiegeLocations().keySet()) {
			Siege s = SiegeService.getInstance().getSiege(i);
			if (s != null) {
				int secondsLeft = SiegeService.getInstance().getRemainingSiegeTimeInSeconds(i);
				if (secondsLeft > 0) {
					String minSec = secondsLeft / 60 + "m ";
					minSec += secondsLeft % 60 + "s";
					sb.append("Location: ").append(i).append(": ").append(minSec).append(" left.");
				}
			}
		}
		if (sb.length() == 0)
			sendInfo(player, "There are currently no active sieges.");
		else
			sendInfo(player, sb.toString());
	}

	private void capture(Player player, String[] params) {
		if (params.length < 3 || !NumberUtils.isNumber(params[1])) {
			showHelp(player);
			return;
		}

		int siegeLocationId = NumberUtils.toInt(params[1]);
		if (!SiegeService.getInstance().getSiegeLocations().keySet().contains(siegeLocationId)) {
			sendInfo(player, "Invalid Siege Location Id: " + siegeLocationId);
			return;
		}

		// check if params2 is siege race
		SiegeRace sr = null;
		try {
			sr = SiegeRace.valueOf(params[2].toUpperCase());
		} catch (IllegalArgumentException ignored) {
		}

		// try to find legion by name
		Legion legion = null;
		if (sr == null) {
			try {
				int legionId = Integer.valueOf(params[2]);
				legion = LegionService.getInstance().getLegion(legionId);
			} catch (NumberFormatException e) {
				String legionName = "";
				for (int i = 2; i < params.length; i++)
					legionName += " " + params[i];
				legion = LegionService.getInstance().getLegion(legionName.trim());
			}

			if (legion != null) {
				int legionBGeneral = LegionService.getInstance().getBrigadeGeneralOfLegion(legion.getLegionId());
				if (legionBGeneral != 0) {
					PlayerCommonData bGeneral = PlayerService.getOrLoadPlayerCommonData(legionBGeneral);
					sr = SiegeRace.getByRace(bGeneral.getRace());
				}
			}
		}

		// check if can capture
		if (legion == null && sr == null) {
			sendInfo(player, params[2] + " is not valid siege race or legion name");
			return;
		}

		SiegeService.getInstance().captureSiege(sr, legion != null ? legion.getLegionId() : 0, siegeLocationId);
	}

	private void assault(Player player, String[] params) {
		if (params.length < 2 || (!NumberUtils.isNumber(params[1]) && !NumberUtils.isNumber(params[2]))) {
			showHelp(player);
			return;
		}

		int siegeLocationId = NumberUtils.toInt(params[1]);
		int delay = NumberUtils.toInt(params[2]);
		if (!SiegeService.getInstance().getSiegeLocations().keySet().contains(siegeLocationId)) {
			sendInfo(player, "Invalid Siege Location Id: " + siegeLocationId);
			return;
		}

		BalaurAssaultService.getInstance().startAssault(player, siegeLocationId, delay);
	}

	private void showHelp(Player player) {
		sendInfo(player);
		Stream<String> fortressIds = SiegeService.getInstance().getFortresses().keySet().stream().map(String::valueOf);
		Stream<String> artifactIds = SiegeService.getInstance().getStandaloneArtifacts().stream().map(loc -> String.valueOf(loc.getLocationId()));
		Stream<String> outpostIds = SiegeService.getInstance().getOutposts().keySet().stream().map(String::valueOf);
		sendInfo(player, "Fortress: " + fortressIds.collect(Collectors.joining(", ")), "Artifacts: " + artifactIds.collect(Collectors.joining(", ")),
			"Outposts: " + outpostIds.collect(Collectors.joining(", ")));
	}

}
