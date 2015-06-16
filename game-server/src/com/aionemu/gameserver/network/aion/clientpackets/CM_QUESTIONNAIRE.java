package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.HTMLService;

/**
 * @author xTz
 */
public class CM_QUESTIONNAIRE extends AionClientPacket {

	private int objectId;
	private int itemId;
	@SuppressWarnings("unused")
	private String stringItemsId;
	private int itemSize;
	private List<Integer> items;

	public CM_QUESTIONNAIRE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.commons.network.packet.BaseClientPacket#readImpl()
	 */
	@Override
	protected void readImpl() {
		objectId = readD();
		itemSize = readH();
		items = new ArrayList<Integer>();
		for (int i = 0; i < itemSize; i++) {
			itemId = readD();
			items.add(itemId);
		}
		stringItemsId = readS();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.commons.network.packet.BaseClientPacket#runImpl()
	 */
	@Override
	protected void runImpl() {
		if (objectId > 0) {
			Player player = getConnection().getActivePlayer();
			HTMLService.getReward(player, objectId, items);
		}
	}
}
