package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemMoveService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author alexa026, kosyachok
 */
public class CM_MOVE_ITEM extends AionClientPacket {

	/**
	 * Target object id that client wants to TALK WITH or 0 if wants to unselect
	 */
	private int targetObjectId;
	private byte source;
	private byte destination;
	private short slot;

	public CM_MOVE_ITEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();// empty
		source = readSC(); // FROM (0 - player inventory, 1 - regular warehouse, 2 - account warehouse, 3 - legion
												// warehouse)
		destination = readSC(); // TO
		slot = readSH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		
		if(player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MOVE_ITEMS) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}
		
		ItemMoveService.moveItem(player, targetObjectId, source, destination, slot);
	}
}
