package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Sweetkr
 */
public class CM_CUSTOM_SETTINGS extends AionClientPacket {

	private int display;
	private int deny;

	public CM_CUSTOM_SETTINGS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		/**
		 * 1 : show legion mantle 2 : priority equipment 4 : show helmet
		 */
		display = readUH();
		/**
		 * 1 : view detail player 2 : trade 4 : party/force 8 : legion 16 : friend 32 : dual(pvp)
		 */
		deny = readUH();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		activePlayer.getPlayerSettings().setDisplay(display);
		activePlayer.getPlayerSettings().setDeny(deny);

		PacketSendUtility.broadcastPacket(activePlayer, new SM_CUSTOM_SETTINGS(activePlayer), true);
	}
}
