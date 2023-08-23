package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.clientpackets.CM_CHARACTER_EDIT;

/**
 * @author IlBuono
 */
public class SM_PLASTIC_SURGERY extends AionServerPacket {

	private int playerObjId;
	private boolean hasTicket;
	private boolean isGenderSwitch;

	public SM_PLASTIC_SURGERY(Player player, boolean isGenderSwitch) {
		this.playerObjId = player.getObjectId();
		this.hasTicket = CM_CHARACTER_EDIT.checkOrRemoveTicket(player, isGenderSwitch, false);
		this.isGenderSwitch = isGenderSwitch;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeC(hasTicket ? 1 : 2);
		writeC(isGenderSwitch ? 1 : 0);
	}
}
