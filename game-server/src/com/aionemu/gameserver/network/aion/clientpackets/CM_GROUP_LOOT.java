package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.drop.DropDistributionService;

/**
 * @author Rhys2002
 */
public class CM_GROUP_LOOT extends AionClientPacket {

	@SuppressWarnings("unused")
	private int groupId;
	private int index;
	@SuppressWarnings("unused")
	private int unk1;
	private int itemId;
	@SuppressWarnings("unused")
	private int unk2;
	@SuppressWarnings("unused")
	private int unk3;
	private int npcId;
	private int distributionId;
	private int roll;
	private long bid;
	@SuppressWarnings("unused")
	private int unk4;
	@SuppressWarnings("unused")
	private int unk5;

	public CM_GROUP_LOOT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		groupId = readD();
		index = readD();
		unk1 = readD();
		itemId = readD();
		unk2 = readC();
		unk3 = readC(); // 3.0
		unk4 = readC(); // 3.5
		npcId = readD();
		distributionId = readC();// 2: Roll 3: Bid
		roll = readD();// 0: Never Rolled 1: Rolled
		bid = readQ();// 0: No Bid else bid amount
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null) {
			return;
		}
		switch (distributionId) {
			case 2:
				DropDistributionService.getInstance().handleRoll(player, roll, itemId, npcId, index);
				break;
			case 3:
				DropDistributionService.getInstance().handleBid(player, bid, itemId, npcId, index);
				break;
		}
	}
}
