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

	public CM_ATREIAN_PASSPORT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		int count = readH();
		for (int i = 0; i < count || count == -1; i++) {
			if (getRemainingBytes() < 8) {
				if (count != -1)
					LoggerFactory.getLogger(CM_ATREIAN_PASSPORT.class).warn("Received invalid passport count " + count + " with only data for " + i
						+ " passports from " + getConnection().getActivePlayer() + "\nCurrent passport data: " + passports);
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
