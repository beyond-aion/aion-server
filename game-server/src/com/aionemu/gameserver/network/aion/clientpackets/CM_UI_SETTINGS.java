package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author ATracer
 */
public class CM_UI_SETTINGS extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_UI_SETTINGS.class);
	private byte settingsType;
	private byte[] data;
	@SuppressWarnings("unused")
	private int size;

	public CM_UI_SETTINGS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		settingsType = readC();
		readH();
		size = readUH();
		data = readB(getRemainingBytes());
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		switch (settingsType) {
			case 0:
				player.getPlayerSettings().setUiSettings(data);
				break;
			case 1:
				player.getPlayerSettings().setShortcuts(data);
				break;
			case 2:
				player.getPlayerSettings().setHouseBuddies(data);
				break;
			default:
				log.warn(player + " sent unknown type of player settings: " + settingsType);
		}
	}
}
