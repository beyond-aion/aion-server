package admincommands;

import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHERABLE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author KID, Rolandas, Neon
 */
public class SpawnUpdate extends AdminCommand {

	public SpawnUpdate() {
		super("spawnu", "Updates spawn data.");

		setSyntaxInfo(
			"<x|y|z|h> [value] - Update X, Y, or Z coordinate or the heading of the selected npc/gatherable (default: takes your current position, optional: the specified value).",
			"<xyz|xyzh> - Update position or position and heading of the selected npc/gatherable to your own one.",
			"<w> [walker_id] - Set walker data of the selected npc (default: remove walker data, optional: set walker id to npc).");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		VisibleObject target = admin.getTarget();
		if (target instanceof Npc && params[0].equalsIgnoreCase("w")) {
			updateWalker(admin, (Npc) target, params.length == 2 ? params[1].toUpperCase() : null);
			return;
		}

		if (!(target instanceof Npc) && !(target instanceof Gatherable)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		Float x = null, y = null, z = null;
		Byte h = null;
		if (params[0].equalsIgnoreCase("xyz")) {
			x = admin.getX();
			y = admin.getY();
			z = admin.getZ();
		} else if (params[0].equalsIgnoreCase("xyzh")) {
			x = admin.getX();
			y = admin.getY();
			z = admin.getZ();
			h = admin.getHeading();
		} else if (params[0].equalsIgnoreCase("x")) {
			if (params.length == 1)
				x = admin.getX();
			else
				x = Float.parseFloat(params[1]);
		} else if (params[0].equalsIgnoreCase("y")) {
			if (params.length == 1)
				y = admin.getY();
			else
				y = Float.parseFloat(params[1]);
		} else if (params[0].equalsIgnoreCase("z")) {
			if (params.length == 1)
				z = admin.getZ();
			else
				z = Float.parseFloat(params[1]);
		} else if (params[0].equalsIgnoreCase("h")) {
			if (params.length == 1)
				h = admin.getHeading();
			else
				h = Byte.parseByte(params[1]);
		} else {
			sendInfo(admin);
			return;
		}

		WorldPosition tPos = target.getPosition();
		tPos.setXYZH(x, y, z, h);

		if (target instanceof Npc npc)
			PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(npc, admin));
		else
			PacketSendUtility.sendPacket(admin, new SM_GATHERABLE_INFO(target));
		sendInfo(admin,
			"Updated " + target.getClass().getSimpleName() + "'s coordinates to\nX:" + tPos.getX() + " Y:" + tPos.getY() + " Z:" + tPos.getZ() + " H:"
				+ tPos.getHeading() + ".");

		if (!DataManager.SPAWNS_DATA.saveSpawn(target, false))
			sendInfo(admin, "Could not save spawn. Maybe it's a special or temporary spawn (siege, base, invasion, ...) which cannot be altered.");
	}

	private void updateWalker(Player admin, Npc target, String walkerId) {
		SpawnTemplate spawn = target.getSpawn();
		String oldId = spawn.getWalkerId();
		if (oldId == null && walkerId == null) {
			sendInfo(admin, "Npc has no walker_id that could be removed.");
		} else {
			if (walkerId != null) {
				WalkerTemplate template = DataManager.WALKER_DATA.getWalkerTemplate(walkerId);
				if (template == null) {
					sendInfo(admin, "No such template exists in npc_walker.xml.");
					return;
				}
				List<SpawnGroup> allSpawns = DataManager.SPAWNS_DATA.getSpawnsByWorldId(target.getWorldId());
				List<SpawnTemplate> allSpots = allSpawns.stream().flatMap(s -> s.getSpawnTemplates().stream()).collect(Collectors.toList());
				List<SpawnTemplate> sameIds = allSpots.stream().filter(s -> s.getWalkerId().equals(walkerId)).collect(Collectors.toList());
				if (sameIds.size() >= template.getPool()) {
					sendInfo(admin, "Can not assign, walker pool reached the limit.");
					return;
				}
			}
			spawn.setWalkerId(walkerId);
			PacketSendUtility.sendPacket(admin, new SM_DELETE(target));
			PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(target, admin));
			if (walkerId == null)
				sendInfo(admin, "Removed npcs walker_id " + oldId + " for " + target.getNpcId() + ".");
			else
				sendInfo(admin, "Updated npcs walker_id from " + oldId + " to " + walkerId + ".");
			if (!DataManager.SPAWNS_DATA.saveSpawn(target, false))
				sendInfo(admin, "Could not save spawn.");
		}
	}
}
