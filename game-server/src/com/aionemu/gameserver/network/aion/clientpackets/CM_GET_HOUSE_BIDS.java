package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.HouseBids;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_BIDS;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.collections.DynamicServerPacketBodySplitter;
import com.aionemu.gameserver.utils.collections.ListSplitter;

/**
 * @author Rolandas
 */
public class CM_GET_HOUSE_BIDS extends AionClientPacket {

	public CM_GET_HOUSE_BIDS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {

	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		List<HouseBids> houseBids = HousingBidService.getInstance().getBidInfo(player.getRace());
		ListSplitter<HouseBids> splitter = new DynamicServerPacketBodySplitter<>(houseBids, true, SM_HOUSE_BIDS.STATIC_BODY_SIZE,
			SM_HOUSE_BIDS.DYNAMIC_BODY_PART_SIZE_CALCULATOR);
		splitter.forEachRemaining(bids -> PacketSendUtility.sendPacket(player, new SM_HOUSE_BIDS(splitter.isFirstSplit(), splitter.isLastSplit(), bids)));
	}

}
