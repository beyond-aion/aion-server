package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.PrivateStoreService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple
 */
public class CM_PRIVATE_STORE_NAME extends AionClientPacket {

	private String name;

	/**
	 * Constructs new instance of <tt>CM_PRIVATE_STORE </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_PRIVATE_STORE_NAME(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		name = readS();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		
		if(activePlayer.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_PRIVATESTORE) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(activePlayer, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}
		
		PrivateStoreService.openPrivateStore(activePlayer, name);
	}
}
