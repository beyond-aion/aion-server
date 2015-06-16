package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemRemodelService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Sarynth
 */
public class CM_ITEM_REMODEL extends AionClientPacket {

	private int keepItemId;
	private int extractItemId;

	public CM_ITEM_REMODEL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		readD(); // npcId
		keepItemId = readD();
		extractItemId = readD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		final Player activePlayer = getConnection().getActivePlayer();
		
		if(activePlayer.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_REMODEL_ITEMS) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(activePlayer, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}
		
		ItemRemodelService.remodelItem(activePlayer, keepItemId, extractItemId);
	}
}
