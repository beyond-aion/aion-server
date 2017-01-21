package admincommands;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Neon
 */
public class MoveTo extends AdminCommand {

	public MoveTo() {
		super("moveto", "Moves you to any location.");

		setParamInfo(
			"<map name|ID> <x> <y> [z] - Moves you to the specified position (map names need underscores instead of spaces).",
			"<position link> - Moves you to the position of the chat link.",
			"<player name> - Moves you to the position of the player.",
			"<npc name|ID> - Moves you to the position of the npc."
		);
	}

	@Override
	public void execute(Player admin, String... params) {
		String errorMsg = null;

		if (params.length == 1 || params.length >= 3 && NumberUtils.isParsable(params[1])) {
			WorldPosition pos = ChatUtil.getPosition(params);
			if (pos != null && pos.getZ() != 0) {
				pos.setH(admin.getHeading());
				moveTo(admin, pos, "Teleported to " + WorldMapType.getWorld(pos.getMapId()) + "\nX:" + pos.getX() + " Y:" + pos.getY() + " Z:"
					+ pos.getZ());
				return;
			} else if (pos != null || params.length > 1 || params[0].startsWith("["))
				errorMsg = "Invalid map position or missing/deactivated geo.";
		}

		if (params.length == 1 && !NumberUtils.isDigits(params[0])) {
			Player player = World.getInstance().findPlayer(Util.convertName(params[0]));
			if (player != null && !player.equals(admin)) {
				moveTo(admin, player.getPosition(), "Teleported to " + ChatUtil.name(player) + ".");
				return;
			} else if (errorMsg == null || player != null)
				errorMsg = "Invalid player name or player is offline.";
		}

		if (params.length >= 1) {
			int npcId = getNpcId(params);
			if (npcId > 0) {
				sendInfo(admin, "Teleported to " + ChatUtil.path(npcId) + ".");
				TeleportService.teleportToNpc(admin, npcId);
				return;
			} else if (errorMsg == null)
				errorMsg = "Could not find the specified npc.";
		}

		sendInfo(admin, errorMsg);
	}

	private void moveTo(Player admin, WorldPosition pos, String message) {
		sendInfo(admin, message); // msg before teleport, otherwise client could ignore it
		TeleportService.teleportTo(admin, pos);
	}

	private int getNpcId(String... params) {
		if (NumberUtils.isDigits(params[0])) {
			int npcId = NumberUtils.toInt(params[0]);
			if (npcId > 0 && DataManager.SPAWNS_DATA.getFirstSpawnByNpcId(0, npcId) != null)
				return npcId;
		} else {
			String npcName = StringUtils.join(params, ' ').toLowerCase();
			for (NpcTemplate template : DataManager.NPC_DATA.getNpcData().valueCollection()) {
				if (template.getName().toLowerCase().equals(npcName)) {
					if (DataManager.SPAWNS_DATA.getFirstSpawnByNpcId(0, template.getTemplateId()) != null)
						return template.getTemplateId();
				}
			}
		}
		return 0;
	}
}
