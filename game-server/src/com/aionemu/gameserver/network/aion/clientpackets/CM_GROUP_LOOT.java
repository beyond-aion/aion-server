package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

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
	private int npcObjId;
	private int distributionMode;
	private int roll;
	private long bid;
	@SuppressWarnings("unused")
	private int unk4;
	@SuppressWarnings("unused")
	private int unk5;

	public CM_GROUP_LOOT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		groupId = readD();
		index = readD();
		unk1 = readD();
		itemId = readD();
		unk2 = readUC();
		unk3 = readUC(); // 3.0
		unk4 = readUC(); // 3.5
		npcObjId = readD();
		distributionMode = readUC();// 2: Roll 3: Bid
		roll = readD();// 0: Never Rolled 1: Rolled
		bid = readQ();// 0: No Bid else bid amount
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;
		DropDistributionService.getInstance().handleRollOrBid(player, distributionMode, roll, bid, itemId, npcObjId, index);
	}
}
