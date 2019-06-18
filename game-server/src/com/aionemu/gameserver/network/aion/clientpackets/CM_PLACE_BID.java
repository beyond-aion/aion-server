package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.HousingBidService;

/**
 * @author Rolandas
 */
public class CM_PLACE_BID extends AionClientPacket {

	private int listIndex;
	private long bidOffer;

	public CM_PLACE_BID(int opcode, Set<State> validStates) {
		super(opcode, validStates);
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
			HousingBidService.getInstance().bid(player, listIndex, bidOffer);
		}
	}

}
