package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.HousingBidService;

/**
 * @author Rolandas
 */
public class CM_PLACE_BID extends AionClientPacket {

	int listIndex = 0;
	long bidOffer = 0;

	public CM_PLACE_BID(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		listIndex = readD();
		bidOffer = readQ();
	}

	@Override
	protected void runImpl() {
		if (HousingConfig.ENABLE_HOUSE_AUCTIONS) {
			Player player = getConnection().getActivePlayer();
			HousingBidService.getInstance().placeBid(player, listIndex, bidOffer);
		}
	}

}
