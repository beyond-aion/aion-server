package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Rolandas
 */
public class CM_HOUSE_KICK extends AionClientPacket {

	private byte option;

	public CM_HOUSE_KICK(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		option = readC();
		readH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		House house = player.getActiveHouse();
		if (house == null) {
			AuditLogger.log(player, "tried to kick players from house without owning one");
			return;
		}
		if (option == 1)
			house.getController().kickVisitors(player, false, false);
		else if (option == 2)
			house.getController().kickVisitors(player, true, false);
	}
}
