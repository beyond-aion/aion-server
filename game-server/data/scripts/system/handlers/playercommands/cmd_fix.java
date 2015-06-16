package playercommands;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;


/**
 * @author ViAl
 *
 */
public class cmd_fix extends PlayerCommand {

	private static final String syntax = "Syntax: .fix player_name";
	
	public cmd_fix() {
		super("fix");
	}

	@Override
	public void execute(Player user, String... params) {
		try {
			if(params == null || params.length == 0) {
				PacketSendUtility.sendMessage(user, syntax);
				return;
			}
			String name = params[0];
			if(name.equals(user.getName())) {
				PacketSendUtility.sendMessage(user, "You can't fix yourself. Use other char. "+name);
				return;
			}
			PlayerAccountData playerAccountData = null;
			for(PlayerAccountData p : user.getPlayerAccount().getSortedAccountsList()) {
				if(p.getPlayerCommonData().getName().equals(name)) {
					playerAccountData = p;
				}
			}
			if(playerAccountData == null) {
				PacketSendUtility.sendMessage(user, "Player \""+name+"\" not found on your account.");
				return;
			}
			
			Player player = PlayerService.getPlayer(playerAccountData.getPlayerCommonData().getPlayerObjId(), user.getPlayerAccount());
			TeleportService2.moveToBindLocation(player, false);
			for(Item item : player.getEquipment().getEquippedItems()) {
				if(item.getItemTemplate().isStigma())
					continue;
				item.setEquipped(false);
				DAOManager.getDAO(InventoryDAO.class).store(item, player);
			}
			DAOManager.getDAO(PlayerDAO.class).storePlayer(player);
			player = null;
			PacketSendUtility.sendMessage(user, "Player \""+name+"\" was fixed!");
		}
		catch(Exception e) {
			PacketSendUtility.sendMessage(user, syntax);
		}
	}

	
}
