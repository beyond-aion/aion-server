package admincommands;

import java.util.Comparator;

import com.aionemu.gameserver.model.base.Base;
import com.aionemu.gameserver.model.base.BaseLocation;
import com.aionemu.gameserver.model.base.BaseOccupier;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

public class BaseCommand extends AdminCommand {

	public BaseCommand() {
		super("base", "Lists bases or changes their state.");

		// @formatter:off
		setSyntaxInfo(
			"list - Lists all available base locations with their respective occupier.",
			"start <id> - Activates the specified base.",
			"stop <id> - Deactivates the specified base.",
			"capture <id> <occupier> - Captures the specified base with the specified new occupier.",
			"assault <id> - Spawns attacker NPCs for the specified base if available."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		switch (params[0].toLowerCase()) {
			case "list" -> showBaseLocationList(player);
			case "start" -> startBase(player, params);
			case "stop" -> stopBase(player, params);
			case "capture" -> captureBase(player, params);
			case "assault" -> assaultBase(player, params);
			default -> sendInfo(player);
		}
	}

	private void showBaseLocationList(Player player) {
		BaseService.getInstance().getBaseLocations().stream()
			.sorted(Comparator.comparingInt(BaseLocation::getId))
			.forEach(loc -> sendInfo(player, "Base %d belongs to %s".formatted(loc.getId(), loc.getOccupier())));
	}

	private void startBase(Player player, String[] params) {
		int baseId = parseBaseId(player, params, 2);
		if (baseId == 0)
			return;

		if (BaseService.getInstance().isActive(baseId)) {
			sendInfo(player, "Base is already active");
			return;
		}
		BaseService.getInstance().start(baseId);
	}

	private void stopBase(Player player, String[] params) {
		int baseId = parseBaseId(player, params, 2);
		if (baseId == 0)
			return;

		if (!BaseService.getInstance().isActive(baseId)) {
			sendInfo(player, "Base is already inactive.");
			return;
		}
		BaseService.getInstance().stop(baseId);
	}

	protected void captureBase(Player player, String[] params) {
		int baseId = parseBaseId(player, params, 3);
		if (baseId == 0)
			return;

		if (!BaseService.getInstance().isActive(baseId)) {
			sendInfo(player, "Inactive bases cannot be captured.");
			return;
		}

		BaseOccupier occupier = BaseOccupier.valueOf(params[2].toUpperCase());
		BaseService.getInstance().capture(baseId, occupier);
	}

	protected void assaultBase(Player player, String[] params) {
		int baseId = parseBaseId(player, params, 3);
		if (baseId == 0)
			return;

		Base<?> base = BaseService.getInstance().getActiveBase(baseId);
		if (base == null) {
			sendInfo(player, "Inactive bases cannot be assaulted.");
			return;
		}
		if (base.isUnderAssault()) {
			sendInfo(player, "Base is already under assault.");
			return;
		}
		BaseOccupier occupier = BaseOccupier.valueOf(params[2].toUpperCase());
		if (base.getOccupier() == occupier) {
			sendInfo(player, "Base cannot be assaulted by the same occupier");
			return;
		}
		base.spawnBySpawnHandler(SpawnHandlerType.ATTACKER, occupier);
	}

	private int parseBaseId(Player admin, String[] params, int expectedParameters) {
		if (params.length < expectedParameters) {
			sendInfo(admin, "Not enough parameters.");
			return 0;
		}

		int baseId = Integer.parseInt(params[1]);
		if (BaseService.getInstance().getBaseLocation(baseId) == null) {
			sendInfo(admin, "Invalid base ID.");
			return 0;
		}

		return baseId;
	}
}
