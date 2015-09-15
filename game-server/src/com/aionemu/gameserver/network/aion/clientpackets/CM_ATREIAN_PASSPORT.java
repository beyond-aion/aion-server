package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.AtreianPassportService;

/**
 * @author ViAl
 */
public class CM_ATREIAN_PASSPORT extends AionClientPacket {

	private int count;
	private List<Integer> passportId = new FastTable<Integer>();
	private List<Integer> timestamps = new FastTable<Integer>();

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
		count = readH();
		for (int i = 0; i < count; i++) {
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
