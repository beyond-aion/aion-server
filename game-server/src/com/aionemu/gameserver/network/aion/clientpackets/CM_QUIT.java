package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerPunishmentsDAO;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.player.PlayerLeaveWorldService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author -Nemesiss-
 * @modified Neon
 */
public class CM_QUIT extends AionClientPacket {

	/**
	 * if true, player wants to go to the character selection or plastic surgery screen.
	 */
	private boolean stayConnected;

	/**
	 * Constructs new instance of <tt>CM_QUIT</tt> packet
	 * 
	 * @param opcode
	 */
	public CM_QUIT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		stayConnected = readC() == 1;
	}

	@Override
	protected void runImpl() {
		AionConnection con = getConnection();
		Player player = con.getActivePlayer();
		boolean charEditScreen = false;

		if (player != null) {
			if (stayConnected) { // update char selection info
				Account account = con.getAccount();
				account.getPlayerAccountData(player.getObjectId()).setEquipment(player.getEquipment().getEquippedForAppearence());
				for (PlayerAccountData plAccData : account.getSortedAccountsList())
					plAccData.setCharBanInfo(DAOManager.getDAO(PlayerPunishmentsDAO.class).getCharBanInfo(plAccData.getPlayerCommonData().getPlayerObjId()));
			}
			charEditScreen = player.isInEditMode();
			PlayerLeaveWorldService.leaveWorld(player);
		}

		sendPacket(new SM_QUIT_RESPONSE(charEditScreen));

		if (!stayConnected) {
			// delay to avoid client disconnect error
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					LoginServer.getInstance().aionClientDisconnected(con.getAccount().getId());
					con.close();
				}
			}, 50);
		}
	}
}
