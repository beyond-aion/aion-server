package admincommands;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.services.CommandsAccessService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ViAl, Neon
 */
public class Access extends AdminCommand {

	private final Map<Integer, Byte> oldAccessLevels = new HashMap<>();

	public Access() {
		super("access", "Chat command and access level management.");

		// @formatter:off
		setSyntaxInfo(
			"<add> <player name> <command name> - Grants the player access to the given chat command.",
			"<remove> <player name> <command name> - Removes the player's access to the given chat command.",
			"<removeall> <player name> - Removes all granted accesses.",
			"<level> <number> - Temporarily sets your accesslevel to a given lower value (for test purposes).",
			"<level> <reset> - Resets your accesslevel to the original value."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length <= 1) {
			sendInfo(admin);
			return;
		}
		String cmd = params[0].toLowerCase();
		switch (cmd) {
			case "add":
			case "remove": {
				if (params.length < 3) {
					sendInfo(admin);
					return;
				}
				String playerName = params[1];
				String commandName = params[2];
				PlayerCommonData pcd = PlayerDAO.loadPlayerCommonDataByName(playerName);
				if (pcd == null) {
					sendInfo(admin, "Player with name " + playerName + " doesn't exists!");
					return;
				}
				if (cmd.equals("add"))
					CommandsAccessService.giveAccess(admin, pcd.getPlayerObjId(), commandName);
				else
					CommandsAccessService.removeAccess(admin, pcd.getPlayerObjId(), commandName);
				break;
			}
			case "removeall": {
				String playerName = params[1];
				PlayerCommonData pcd = PlayerDAO.loadPlayerCommonDataByName(playerName);
				if (pcd == null) {
					sendInfo(admin, "Player with name " + playerName + " doesn't exists!");
					return;
				}
				if (CommandsAccessService.removeAllAccesses(pcd.getPlayerObjId()))
					sendInfo(admin, "Removed all custom chat command accesses from player " + pcd.getName());
				else
					sendInfo(admin, "Nothing to remove from player " + pcd.getName());
				break;
			}
			case "level": {
				byte currentLevel = admin.getAccount().getAccessLevel();
				byte maxLevel = oldAccessLevels.getOrDefault(admin.getObjectId(), currentLevel);
				if ("reset".equalsIgnoreCase(params[1])) {
					if (maxLevel == currentLevel) {
						sendInfo(admin, "Nothing to reset.");
						return;
					}
					admin.getAccount().setAccessLevel(maxLevel);
					sendInfo(admin, "Your access level has been reset.");
				} else {
					byte level = NumberUtils.toByte(params[1], (byte) -1);
					if (level == -1 || level > maxLevel) {
						sendInfo(admin, "Invalid access level.");
						return;
					}
					if (level == currentLevel) {
						sendInfo(admin, "You already have access level " + level + ".");
						return;
					}
					admin.getAccount().setAccessLevel(level);
					if (level < getLevel() && !CommandsAccessService.hasAccess(admin.getObjectId(), getAlias())) {
						CommandsAccessService.giveTemporaryAccess(admin, admin.getObjectId(), getAlias());
					}
					sendInfo(admin, "Your access level has been changed.");
				}
				currentLevel = admin.getAccount().getAccessLevel();
				if (currentLevel == maxLevel) {
					oldAccessLevels.remove(admin.getObjectId());
				} else {
					oldAccessLevels.putIfAbsent(admin.getObjectId(), maxLevel);
				}
				if (currentLevel > getLevel() && CommandsAccessService.hasAccess(admin.getObjectId(), getAlias())) {
					CommandsAccessService.removeAccess(admin, admin.getObjectId(), getAlias());
				}
				admin.getController().onChangedPlayerAttributes();
				break;
			}
			default:
				sendInfo(admin);
		}
	}

}
