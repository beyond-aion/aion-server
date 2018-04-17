package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.AtreianPassportService;

/**
 * @author ViAl
 */
public class CM_ATREIAN_PASSPORT extends AionClientPacket {

	private Map<Integer, Set<Integer>> passports = new HashMap<>();

	public CM_ATREIAN_PASSPORT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		int count = readUH();
		for (int i = 0; i < count; i++) {
			if (getRemainingBytes() == 0) { // debugging purposes due to error on readD
				LoggerFactory.getLogger(CM_ATREIAN_PASSPORT.class)
					.warn("Received invalid passport count (" + count + ") from " + getConnection().getActivePlayer());
				break;
			}
			int passportId = readD();
			int timestamp = readD();
			passports.compute(passportId, (ppId, timestamps) -> {
				if (timestamps == null)
					timestamps = new HashSet<>();
				timestamps.add(timestamp);
				return timestamps;
			});
		}

	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player != null)
			AtreianPassportService.getInstance().takeReward(player, passports);
	}

}
