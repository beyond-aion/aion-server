package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.AtreianPassportService;

/**
 * @author ViAl
 */
public class CM_ATREIAN_PASSPORT extends AionClientPacket {

	private int count;
	private List<Integer> passportId = new ArrayList<>();
	private List<Integer> timestamps = new ArrayList<>();

	/**
	 * @param opcode
	 * @param state
	 * @param restStates
	 */
	public CM_ATREIAN_PASSPORT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		count = readUH();
		for (int i = 0; i < count; i++) {
			if (getRemainingBytes() == 0) { // debugging purposes due to error on readD
				LoggerFactory.getLogger(CM_ATREIAN_PASSPORT.class)
					.warn("Received invalid passport count (" + count + ") from " + getConnection().getActivePlayer());
				break;
			}
			passportId.add(readD());
			timestamps.add(readD());
		}

	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;
		AtreianPassportService.getInstance().takeReward(player, timestamps, passportId);
	}

}
