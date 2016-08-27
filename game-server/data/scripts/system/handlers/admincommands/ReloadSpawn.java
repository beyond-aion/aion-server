package admincommands;

import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Luno, reworked Bobobear
 */
public class ReloadSpawn extends AdminCommand {

	public ReloadSpawn() {
		super("reload_spawn");
	}

	@Override
	public void execute(Player player, String... params) {
		int worldId;
		String destination;

		worldId = 0;
		destination = "null";

		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(player, "syntax //reload_spawn <location name | all>");
		} else {
			StringBuilder sbDestination = new StringBuilder();
			for (String p : params)
				sbDestination.append(p + " ");

			destination = sbDestination.toString().trim();

			if (destination.equalsIgnoreCase("Sanctum"))
				worldId = WorldMapType.SANCTUM.getId();
			else if (destination.equalsIgnoreCase("Kaisinel"))
				worldId = WorldMapType.KAISINEL.getId();
			else if (destination.equalsIgnoreCase("Poeta"))
				worldId = WorldMapType.POETA.getId();
			else if (destination.equalsIgnoreCase("Verteron"))
				worldId = WorldMapType.VERTERON.getId();
			else if (destination.equalsIgnoreCase("Eltnen"))
				worldId = WorldMapType.ELTNEN.getId();
			else if (destination.equalsIgnoreCase("Theobomos"))
				worldId = WorldMapType.THEOBOMOS.getId();
			else if (destination.equalsIgnoreCase("Heiron"))
				worldId = WorldMapType.HEIRON.getId();
			else if (destination.equalsIgnoreCase("Pandaemonium"))
				worldId = WorldMapType.PANDAEMONIUM.getId();
			else if (destination.equalsIgnoreCase("Marchutan"))
				worldId = WorldMapType.MARCHUTAN.getId();
			else if (destination.equalsIgnoreCase("Ishalgen"))
				worldId = WorldMapType.ISHALGEN.getId();
			else if (destination.equalsIgnoreCase("Altgard"))
				worldId = WorldMapType.ALTGARD.getId();
			else if (destination.equalsIgnoreCase("Morheim"))
				worldId = WorldMapType.MORHEIM.getId();
			else if (destination.equalsIgnoreCase("Brusthonin"))
				worldId = WorldMapType.BRUSTHONIN.getId();
			else if (destination.equalsIgnoreCase("Beluslan"))
				worldId = WorldMapType.BELUSLAN.getId();
			else if (destination.equalsIgnoreCase("Inggison"))
				worldId = WorldMapType.INGGISON.getId();
			else if (destination.equalsIgnoreCase("Gelkmaros"))
				worldId = WorldMapType.GELKMAROS.getId();
			else if (destination.equalsIgnoreCase("Silentera"))
				worldId = WorldMapType.SILENTERA_CANYON.getId();
			else if (destination.equalsIgnoreCase("Reshanta"))
				worldId = WorldMapType.RESHANTA.getId();
			else if (destination.equalsIgnoreCase("Kaisinel Academy"))
				worldId = WorldMapType.KAISINEL_ACADEMY.getId();
			else if (destination.equalsIgnoreCase("Marchutan Priory"))
				worldId = WorldMapType.MARCHUTAN_PRIORY.getId();
			else if (destination.equalsIgnoreCase("Oriel"))
				worldId = WorldMapType.ORIEL.getId();
			else if (destination.equalsIgnoreCase("Pernon"))
				worldId = WorldMapType.PERNON.getId();
			else if (destination.equalsIgnoreCase("Kaldor"))
				worldId = WorldMapType.KALDOR.getId();
			else if (destination.equalsIgnoreCase("Levinshor"))
				worldId = WorldMapType.LEVINSHOR.getId();
			else if (destination.equalsIgnoreCase("All"))
				worldId = 0;
			else
				PacketSendUtility.sendMessage(player, "Could not find the specified map !");
		}
		final String destinationMap = destination;

		// despawn specified map, no instance
		if (destination.equalsIgnoreCase("All")) {
			reloadMap(WorldMapType.SANCTUM.getId(), player, "Sanctum");
			reloadMap(WorldMapType.KAISINEL.getId(), player, "Kaisinel");
			reloadMap(WorldMapType.POETA.getId(), player, "Poeta");
			reloadMap(WorldMapType.VERTERON.getId(), player, "Verteron");
			reloadMap(WorldMapType.ELTNEN.getId(), player, "Eltnen");
			reloadMap(WorldMapType.THEOBOMOS.getId(), player, "Theobomos");
			reloadMap(WorldMapType.HEIRON.getId(), player, "Heiron");
			reloadMap(WorldMapType.PANDAEMONIUM.getId(), player, "Pandaemonium");
			reloadMap(WorldMapType.MARCHUTAN.getId(), player, "Marchutan");
			reloadMap(WorldMapType.ISHALGEN.getId(), player, "Ishalgen");
			reloadMap(WorldMapType.ALTGARD.getId(), player, "Altgard");
			reloadMap(WorldMapType.MORHEIM.getId(), player, "Morheim");
			reloadMap(WorldMapType.BRUSTHONIN.getId(), player, "Brusthonin");
			reloadMap(WorldMapType.BELUSLAN.getId(), player, "Beluslan");
			reloadMap(WorldMapType.INGGISON.getId(), player, "Inggison");
			reloadMap(WorldMapType.GELKMAROS.getId(), player, "Gelkmaros");
			reloadMap(WorldMapType.SILENTERA_CANYON.getId(), player, "Silentera");
			reloadMap(WorldMapType.RESHANTA.getId(), player, "Reshanta");
			reloadMap(WorldMapType.KAISINEL_ACADEMY.getId(), player, "Kaisinel Academy");
			reloadMap(WorldMapType.MARCHUTAN_PRIORY.getId(), player, "Marchutan Priory");
			reloadMap(WorldMapType.ORIEL.getId(), player, "Oriel");
			reloadMap(WorldMapType.PERNON.getId(), player, "Pernon");
			reloadMap(WorldMapType.KALDOR.getId(), player, "Kaldor");
			reloadMap(WorldMapType.LEVINSHOR.getId(), player, "Levinshor");
		} else {
			reloadMap(worldId, player, destinationMap);
		}
	}

	private void reloadMap(int worldId, Player admin, String destinationMap) {
		final int IdWorld = worldId;
		final Player adm = admin;
		final String dest = destinationMap;

		if (IdWorld != 0) {
			World.getInstance().forEachObject(v -> {
				if (v.getWorldId() != IdWorld)
					return;
				if (v instanceof Npc || v instanceof Gatherable || v instanceof StaticObject)
					v.getController().onDelete();
			});
			SpawnEngine.spawnWorldMap(IdWorld);
			PacketSendUtility.sendMessage(adm, "Spawns for map: " + IdWorld + " (" + dest + ") reloaded succesfully");
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //reload_spawn <location name | all>");
	}
}
