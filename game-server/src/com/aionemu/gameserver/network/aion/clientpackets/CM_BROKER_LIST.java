package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.BrokerService;

/**
 * @author kosyachok
 */
public class CM_BROKER_LIST extends AionClientPacket {

	@SuppressWarnings("unused")
	private int brokerId;
	private byte sortType;
	private int page;
	private int listMask;

	public CM_BROKER_LIST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		brokerId = readD();
		sortType = readC(); // 1 - name; 2 - level; 4 - totalPrice; 6 - price for piece
		page = readUH();
		listMask = readUH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		BrokerService.getInstance().showRequestedItems(player, listMask, sortType, page, null);
	}
}
