package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author ATracer
 */
public class CM_UI_SETTINGS extends AionClientPacket {

	int settingsType;
	byte[] data;
	int size;

	public CM_UI_SETTINGS(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		settingsType = readC();
		readH();
		size = readH();
		data = readB(getRemainingBytes());
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (settingsType == 0) {
			player.getPlayerSettings().setUiSettings(data);
		} else if (settingsType == 1) {
			player.getPlayerSettings().setShortcuts(data);
		} else if (settingsType == 2) {
			player.getPlayerSettings().setHouseBuddies(data);
		}
	}
}
