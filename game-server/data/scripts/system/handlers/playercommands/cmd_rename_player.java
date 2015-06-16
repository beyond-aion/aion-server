package playercommands;

import java.util.Iterator;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dao.OldNamesDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.ingameshop.InGameShopEn;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RENAME;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;
import com.aionemu.gameserver.world.World;


/**
 * @author ViAl
 *
 */
public class cmd_rename_player extends PlayerCommand {

	private static final int PRICE_TOLL = 150;
	
	public cmd_rename_player() {
		super("rename_player");
	}

	@Override
	public void execute(Player player, String... params) {
		try {
			if(params.length < 1) {
				PacketSendUtility.sendMessage(player, "Syntax: .rename_player NewName");
				return;
			}
			String oldName = player.getName();
			String newName = params[0];
			if (!NameRestrictionService.isValidName(newName)) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400151));
				return;
			}
			if (NameRestrictionService.isForbiddenWord(newName)) {
				PacketSendUtility.sendMessage(player, "You are trying to use a forbidden name. Choose another one!");
				return;
			}
			if (!PlayerService.isFreeName(newName)) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400155));
				return;
			}
			if (player.getName().equals(newName)) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400153));
				return;
			}
			if (!CustomConfig.OLD_NAMES_COUPON_DISABLED && PlayerService.isOldName(newName)) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400155));
				return;
			}
			if(player.getPlayerAccount().getToll() < PRICE_TOLL) {
				PacketSendUtility.sendMessage(player, "You need to have "+PRICE_TOLL+" tolls to rename yourself");
				return;
			}
			InGameShopEn.getInstance().addToll(player, -PRICE_TOLL);
			
			if (!CustomConfig.OLD_NAMES_COUPON_DISABLED)
				DAOManager.getDAO(OldNamesDAO.class).insertNames(player.getObjectId(), player.getName(), newName);
			player.getCommonData().setName(newName);

			Iterator<Player> onlinePlayers = World.getInstance().getPlayersIterator();
			while (onlinePlayers.hasNext()) {
				Player p = onlinePlayers.next();
				if (p != null && p.getClientConnection() != null)
					PacketSendUtility.sendPacket(p, new SM_RENAME(player.getObjectId(), oldName, newName));
			}
			DAOManager.getDAO(PlayerDAO.class).storePlayer(player);
		}
		catch(Exception e) {
			PacketSendUtility.sendMessage(player, "Syntax: .rename_player NewName");
		}
	}

}
