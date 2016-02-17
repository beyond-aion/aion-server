package admincommands;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Luno
 * @modified Neon
 */
public class SpawnNpc extends AdminCommand {

	public SpawnNpc() {
		super("spawn", "Spawns npcs and gatherables.");

		setParamInfo(
			"<id> - Spawns a temporary object with the specified ID.",
			"<id> <static id> [respawn time] - Spawns an object with the specified ID and static ID (default: temporary spawn, optional: respawn time in seconds)."
		);
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}

		int npcId = NumberUtils.toInt(params[0]);
		int staticId = params.length < 2 ? 0 : NumberUtils.toInt(params[1]);
		int respawnTime = params.length < 3 ? 0 : NumberUtils.toInt(params[2]);

		if (npcId == 0) {
			sendInfo(admin, "Invalid npc id.");
			return;
		} else if (DataManager.NPC_DATA.getNpcTemplate(npcId) == null) {
			sendInfo(admin, "Template for npc id " + npcId + " was not found.");
			return;
		}

		SpawnTemplate st = SpawnEngine.addNewSpawn(admin.getWorldId(), npcId, admin.getX(), admin.getY(), admin.getZ(), admin.getHeading(), respawnTime);
		st.setStaticId(staticId);
		VisibleObject visibleObject = SpawnEngine.spawnObject(st, admin.getInstanceId());
		String objectName = visibleObject.getObjectTemplate().getName();
		sendInfo(admin, StringUtils.capitalize(objectName) + " spawned with static id: " + staticId + " and respawn time: " + respawnTime);
		if (respawnTime > 0 && !DataManager.SPAWNS_DATA2.saveSpawn(visibleObject, false))
			sendInfo(admin, "Could not save spawn. Npc will vanish after server restart.");
	}
}
