package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

	public CM_QUESTIONNAIRE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
		itemSize = readUH();
		items = new ArrayList<>();
		for (int i = 0; i < itemSize; i++) {
			itemId = readD();
			items.add(itemId);
		}
		stringItemsId = readS();
	}

	@Override
	protected void runImpl() {
		if (objectId > 0) {
			Player player = getConnection().getActivePlayer();
			HTMLService.getReward(player, objectId, items);
		}
	}
}
