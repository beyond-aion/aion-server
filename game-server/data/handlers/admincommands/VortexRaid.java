package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapType;

public class VortexRaid extends AdminCommand {

	public VortexRaid() {
		super("vortexraid", "Starts/stops a raid in Theobomos or Brusthonin.");

		// @formatter:off
		setSyntaxInfo(
			"start <Theobomos|Brusthonin> - Starts the raid at the given location.",
			"stop <Theobomos|Brusthonin> - Stops the raid at the given location."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 2) {
			sendInfo(player);
			return;
		}

		int mapId = WorldMapType.getMapId(params[1]);
		VortexLocation loc = DataManager.VORTEX_DATA.getVortexLocation(mapId);
		if (loc == null) {
			sendInfo(player, "Invalid location.");
			return;
		}
		String locationName = World.getInstance().getWorldMap(mapId).getName();

		if ("start".equalsIgnoreCase(params[0])) {
			if (VortexService.getInstance().isInvasionInProgress(loc.getId())) {
				sendInfo(player, locationName + " is already under siege.");
			} else {
				sendInfo(player, locationName + " raid started.");
				VortexService.getInstance().startInvasion(loc.getId());
			}
		} else if ("stop".equalsIgnoreCase(params[0])) {
			if (!VortexService.getInstance().isInvasionInProgress(loc.getId())) {
				sendInfo(player, locationName + " is not under siege.");
			} else {
				sendInfo(player, locationName + " raid stopped.");
				VortexService.getInstance().stopInvasion(loc.getId());
			}
		} else {
			sendInfo(player);
		}
	}
}
