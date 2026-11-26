package admincommands;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.configs.main.GeoDataConfig.Mode;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_POSITION;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Neon
 */
public class MoveTo extends AdminCommand {

	private static final Pattern HEADING_PARAM = Pattern.compile("h(\\d{1,3})", Pattern.CASE_INSENSITIVE);

	public MoveTo() {
		super("moveto", "Moves you to any location.", """
			<x> <y> [z] - Moves you to the specified coordinates on the current map (also supports pasted xml attributes like x="1422.7744" y="1250.0612" z="569.47").
			<map name|ID> <x> <y> [z] - Moves you to the specified position (map names need underscores instead of spaces).
			<position link> - Moves you to the position of the chat link.
			<player name> - Moves you to the player.
			<npc name|ID> - Moves you to a spawn spot of the NPC.
			forward <distance> - Moves you forward by the specified distance, ignoring any obstacles in between.
			Coordinates and position links accept a trailing h<0-119> to set your heading after the move (degrees / 3, e.g. h30 = 90°).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}
		String errorMsg = null;
		if (params.length == 2 && "forward".equalsIgnoreCase(params[0])) {
			moveForward(admin, Float.parseFloat(params[1]));
			return;
		}
		byte heading = admin.getHeading();
		Matcher headingParam = HEADING_PARAM.matcher(params[params.length - 1]);
		if (params.length > 1 && headingParam.matches()) {
			int h = Integer.parseInt(headingParam.group(1));
			if (h >= 120) {
				sendInfo(admin, "Heading must be between 0 and 119.");
				return;
			}
			heading = (byte) h;
			params = Arrays.copyOf(params, params.length - 1);
		}
		WorldPosition pos = params.length == 1 ? ChatUtil.getPosition(params[0]) : parseWorldPosition(admin, params);
		if (pos != null) {
			pos.setH(heading);
			moveTo(admin, pos, "Teleported to " + worldName(pos.getMapId()) + "\nX:" + pos.getX() + " Y:" + pos.getY() + " Z:" + pos.getZ());
			return;
		} else if (params.length > 1 || params[0].startsWith("[pos:"))
			errorMsg = "Invalid map position or %s geo.".formatted(GeoDataConfig.MODE == Mode.ON ? "missing" : "deactivated");

		String nameOrId = String.join(" ", params).toLowerCase();
		Player player = World.getInstance().getPlayer(Util.convertName(nameOrId));
		if (player != null && !player.equals(admin)) {
			moveTo(admin, player.getPosition(), "Teleported to " + name(player) + ".");
			return;
		} else if (errorMsg == null || admin.equals(player)) {
			errorMsg = "Invalid player name or player is offline.";
		}

		int npcId = getNpcId(nameOrId);
		if (npcId > 0 && DataManager.SPAWNS_DATA.getFirstSpawnByNpcId(0, npcId) != null) {
			sendInfo(admin, "Teleported to " + ChatUtil.path(npcId, true) + ".");
			TeleportService.teleportToNpc(admin, npcId);
			return;
		} else if (npcId > 0) {
			errorMsg = "Could not find " + ChatUtil.path(npcId, true) + ".";
		} else if (nameOrId.contains(" ")) {
			errorMsg = "Could not find \"" + nameOrId + "\".";
		}

		sendInfo(admin, errorMsg);
	}

	private void moveForward(Player admin, float distance) {
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(admin.getHeading()));
		float newX = (float) (admin.getX() + Math.cos(radian) * distance);
		float newY = (float) (admin.getY() + Math.sin(radian) * distance);
		admin.getPosition().setXYZH(newX, newY, admin.getZ(), admin.getHeading());
		PacketSendUtility.broadcastToSightedPlayers(admin, new SM_POSITION(admin), true);
	}

	private WorldPosition parseWorldPosition(Player admin, String[] params) {
		int coordIndex = 0;
		int mapId;
		boolean isMapId = params[0].matches("[1-9][0-9]{8,}");
		boolean isMapName = params[0].matches("[a-zA-Z_]+");
		if (isMapId || isMapName) {
			mapId = isMapId ? Integer.parseInt(params[0]) : WorldMapType.getMapId(params[0]);
			coordIndex = 1;
		} else {
			mapId = admin.getWorldId() + admin.getInstanceId() - 1;
		}
		Float x = null, y = null, z = null;
		Pattern p = Pattern.compile("^((?<type>x|y|z)(=|:)\"?)?(?<coord>[0-9]+(\\.[0-9]+)?f?)\"?,?$", Pattern.CASE_INSENSITIVE);
		int maxIndex = Math.min(params.length, coordIndex + 3);
		for (int i = coordIndex; i < maxIndex; i++) {
			Matcher m = p.matcher(params[i]);
			if (m.find()) {
				float coord = Float.parseFloat(m.group("coord"));
				String type = m.group("type");
				if ("x".equalsIgnoreCase(type) || x == null && type == null)
					x = coord;
				else if ("y".equalsIgnoreCase(type) || y == null && type == null)
					y = coord;
				else if ("z".equalsIgnoreCase(type) || z == null && type == null)
					z = coord;
			} else {
				return null;
			}
		}
		return x == null || y == null ? null : ChatUtil.parsedCoordsToWorldPosition(mapId, x, y, z, null);
	}

	private void moveTo(Player admin, WorldPosition pos, String message) {
		sendInfo(admin, message); // msg before teleport, otherwise client could ignore it
		if (pos.isInstanceMap() && pos.getWorldMapInstance().getParent().getMainWorldMapInstance() == pos.getWorldMapInstance()) {
			// currently instance type maps have a main world map instance (default) which have no spawns, so we create a new instance instead
			WorldMapInstance instance = InstanceService.getOrRegisterInstance(pos.getMapId(), admin);
			pos = World.getInstance().createPosition(pos.getMapId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), instance.getInstanceId());
		}
		TeleportService.teleportTo(admin, pos);
	}

	private int getNpcId(String nameOrId) {
		if (nameOrId.matches("[1-9][0-9]{5}"))
			return Integer.parseInt(nameOrId);
		for (NpcTemplate template : DataManager.NPC_DATA.getNpcData()) {
			if (template.getName().toLowerCase().equals(nameOrId))
				return template.getTemplateId();
		}
		return 0;
	}
}
