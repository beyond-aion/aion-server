package playercommands;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.ingameshop.InGameShopEn;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;


/**
 * @author ViAl
 *
 */
public class cmd_rename_legion extends PlayerCommand {
	
	private static final int PRICE_TOLL = 150;
	
	public cmd_rename_legion() {
		super("rename_legion");
	}

	@Override
	public void execute(Player player, String... params) {
		try {
			if(params.length < 1) {
				PacketSendUtility.sendMessage(player, "Syntax: .rename_legion NewName");
				return;
			}
			String newName = params[0];
			if (!player.isLegionMember()) {
				PacketSendUtility.sendMessage(player, "You are not legion member.");
				return;
			}
			if (!LegionService.getInstance().isValidName(newName)) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400152));
				return;
			}
			if (NameRestrictionService.isForbiddenWord(newName)) {
				PacketSendUtility.sendMessage(player, "You are trying to use a forbidden name. Choose another one!");
				return;
			}
			if (DAOManager.getDAO(LegionDAO.class).isNameUsed(newName)) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400156));
				return;
			}
			if (player.getLegion().getLegionName().equals(newName)) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400154));
				return;
			}
			if(LegionService.getInstance().getLegionBGeneral(player.getLegion().getLegionId()) != player.getObjectId()) {
				PacketSendUtility.sendMessage(player, "Only Brigade General could rename legion.");
				return;
			}
			if(player.getPlayerAccount().getToll() < PRICE_TOLL) {
				PacketSendUtility.sendMessage(player, "You need to have "+PRICE_TOLL+" tolls to rename legion");
				return;
			}
			InGameShopEn.getInstance().addToll(player, -PRICE_TOLL);
			LegionService.getInstance().setLegionName(player.getLegion(), newName, true);
		}
		catch(Exception e) {
			PacketSendUtility.sendMessage(player, "Syntax: .rename_legion NewName");
		}
	}

}
