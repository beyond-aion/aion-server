package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.RiftService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

public class Rift extends AdminCommand {

	public Rift() {
		super("rift", "Opens or closes rifts in the world.", """
			list - Lists all rift locations.
			open <location ID|world ID> [g] - Opens the rifts at the given location. If g is specified and spawns are defined, guards will spawn.
			close <location ID|world ID> - Closes the rifts at the given location.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length > 0 && "list".equalsIgnoreCase(params[0])) {
			sendInfo(player, "Rift locations:");
			RiftService.getInstance().getRiftLocations().forEach(
				(id, loc) -> sendInfo(player, "ID: " + id + ", world ID: " + loc.getWorldId() + (loc.isOpened() ? " (open)" : "")));
		} else if (params.length > 1 && "open".equalsIgnoreCase(params[0])) {
			int id = parseId(params[1]);
			boolean guards = params.length > 2 && params[2].equalsIgnoreCase("g");
			boolean result = RiftService.getInstance().openRifts(id, guards);
			sendInfo(player, result ? "Opened rifts at location " + id + "." : "Rifts are already open.");
		} else if (params.length > 1 && "close".equalsIgnoreCase(params[0])) {
			int id = parseId(params[1]);
			boolean result = RiftService.getInstance().closeRifts(parseId(params[1]));
			sendInfo(player, result ? "Closed rifts at location " + id + "." : "Rifts were already closed.");
		} else {
			sendInfo(player);
		}
	}

	private int parseId(String idParam) {
		int id = Integer.parseInt(idParam);
		if (!RiftService.getInstance().isValidId(id))
			throw new IllegalArgumentException("Invalid rift world ID or location ID.");
		return id;
	}
}
