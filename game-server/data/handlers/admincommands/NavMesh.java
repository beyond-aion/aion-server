package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.navmesh.NavMeshService;
import org.recast4j.detour.StraightPathItem;

import java.util.ArrayList;
import java.util.List;

public class NavMesh extends AdminCommand {

	public NavMesh() {
		super("navmesh", "Calculates navmesh path between you and target");

		// @formatter:off
		setSyntaxInfo(
			" - Calculates navmesh path between you and target."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length > 0) {
			List<WorldMapTemplate> maps = new ArrayList<>();
			DataManager.WORLD_MAPS_DATA.forEach(map -> {
				if (NavMeshService.getInstance().mapSupportsNavMesh(map.getMapId())) {
					maps.add(map);
				}
			});
			StringBuilder sb = new StringBuilder();
			sb.append(maps.size()).append(" maps support navmeshes:\n");
			for (WorldMapTemplate map : maps) {
				sb.append(map.getMapId()).append(" - ").append(map.getName()).append("\n");
			}
			sendInfo(player, sb.toString());
		} else if (NavMeshService.getInstance().mapSupportsNavMesh(player.getWorldId())) {
			VisibleObject target = player.getTarget();
			if (target != null) {
				List<StraightPathItem> items = NavMeshService.getInstance().calculatePath(player, target);
				if (items != null) {
					for (StraightPathItem item : items) {
						SpawnTemplate st = SpawnEngine.newSpawn(player.getWorldId(), 200000, item.getPos()[2], item.getPos()[0], item.getPos()[1], player.getHeading(), 0);
						VisibleObject visibleObject = SpawnEngine.spawnObject(st, player.getInstanceId());
						sendInfo(player, "Spawned npc at: " + visibleObject.getPosition().getX() + " " + visibleObject.getPosition().getY() + " " + visibleObject.getPosition().getZ());
					}
				} else {
					sendInfo(player, "Straight Path list is null");
				}
			} else {
				sendInfo(player, "Target is null");
			}
		} else {
			sendInfo(player, "Your current map does not support navmeshes.");
		}
	}
}