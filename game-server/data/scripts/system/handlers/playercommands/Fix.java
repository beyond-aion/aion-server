package playercommands;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author ViAl
 * @modified Neon
 */
public class Fix extends PlayerCommand {

	public Fix() {
		super("fix", "Fixes a bugged player on your account.");

		setParamInfo("<player name> - Moves the player to bind location and resets equip.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length == 0) {
			sendInfo(player);
			return;
		}

		String name = Util.convertName(params[0]);
		if (name.equals(player.getName())) {
			sendInfo(player, "You can't fix yourself in online state. Use other char.");
			return;
		}

		Player playerToFix = null;
		for (PlayerAccountData p : player.getPlayerAccount().getSortedAccountsList()) {
			if (p.getPlayerCommonData().getName().equals(name)) {
				playerToFix = PlayerService.getPlayer(p.getPlayerCommonData().getPlayerObjId(), player.getPlayerAccount());
				break;
			}
		}
		if (playerToFix == null) {
			sendInfo(player, "Player " + name + " was not found on your account.");
			return;
		}

		TeleportService2.moveToBindLocation(playerToFix, false);
		for (Item item : playerToFix.getEquipment().getEquippedItems()) {
			if (item.getItemTemplate().isStigma())
				continue;
			item.setEquipped(false);
			DAOManager.getDAO(InventoryDAO.class).store(item, playerToFix);
		}
		DAOManager.getDAO(PlayerDAO.class).storePlayer(playerToFix);
		playerToFix = null;
		sendInfo(player, "Player " + name + " was fixed.");
	}
}
