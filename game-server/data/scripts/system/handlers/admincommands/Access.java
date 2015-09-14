package admincommands;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.services.CommandsAccessService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ViAl
 */
public class Access extends AdminCommand {

	private static final String SYNTAX = "Syntax: //access add <player_name> <command_name>\n" + "//access remove <player_name> <command_name>\n"
		+ "//access remove_all <player_name>";

	public Access() {
		super("access");
	}

	@Override
	public void execute(Player admin, String... params) {
		try {
			String cmd = params[0];
			switch (cmd) {
				case "add": {
					String playerName = params[1];
					String commandName = params[2];
					PlayerCommonData pcd = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonDataByName(playerName);
					if (pcd == null) {
						PacketSendUtility.sendMessage(admin, "Player with name " + playerName + " doesn't exists!");
						return;
					}
					CommandsAccessService.getInstance().giveAccess(admin, pcd.getPlayerObjId(), commandName);
				}
					break;
				case "remove": {
					String playerName = params[1];
					String commandName = params[2];
					PlayerCommonData pcd = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonDataByName(playerName);
					if (pcd == null) {
						PacketSendUtility.sendMessage(admin, "Player with name " + playerName + " doesn't exists!");
						return;
					}
					CommandsAccessService.getInstance().removeAccess(admin, pcd.getPlayerObjId(), commandName);
				}
					break;
				case "remove_all": {
					String playerName = params[1];
					PlayerCommonData pcd = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonDataByName(playerName);
					if (pcd == null) {
						PacketSendUtility.sendMessage(admin, "Player with name " + playerName + " doesn't exists!");
						return;
					}
					CommandsAccessService.getInstance().removeAllAccesses(pcd.getPlayerObjId());
					PacketSendUtility.sendMessage(admin, "All accesses successfully removed from player " + playerName);
				}
					break;
				default:
					PacketSendUtility.sendMessage(admin, SYNTAX);
			}
		} catch (Exception e) {
			PacketSendUtility.sendMessage(admin, SYNTAX);
		}
	}

}
