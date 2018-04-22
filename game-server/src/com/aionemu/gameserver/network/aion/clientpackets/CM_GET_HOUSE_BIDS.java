package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.HouseBidEntry;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_BIDS;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.utils.PacketSendUtility;
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
		// No data
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		HouseBidEntry playerBid = HousingBidService.getInstance().getLastPlayerBid(player.getObjectId());

		// TODO: [RR] check player bids placement in sniffs, it's just a guess ;)
		List<HouseBidEntry> houseBids = HousingBidService.getInstance().getHouseBidEntries(player.getRace());
		ListSplitter<HouseBidEntry> splitter = new ListSplitter<>(houseBids, 181, true);
		while (splitter.hasMore()) {
			List<HouseBidEntry> packetBids = splitter.getNext();
			HouseBidEntry playerData = splitter.isLast() ? playerBid : null;
			PacketSendUtility.sendPacket(player, new SM_HOUSE_BIDS(splitter.isFirst(), splitter.isLast(), playerData, packetBids));
		}
	}

}
