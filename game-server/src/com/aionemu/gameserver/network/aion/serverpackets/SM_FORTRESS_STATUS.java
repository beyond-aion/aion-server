package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;

import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.SiegeService;

public class SM_FORTRESS_STATUS extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		Map<Integer, FortressLocation> fortresses = SiegeService.getInstance().getFortresses();
		Influence inf = Influence.getInstance();

		writeC(1);
		writeD(SiegeService.getInstance().getSecondsUntilNextFortressState());
		writeF(inf.getElyosInfluenceRate());
		writeF(inf.getAsmodianInfluenceRate());
		writeF(inf.getBalaurInfluenceRate());
		writeH(inf.getInfluenceRelevantWorldIds().size());
		for (int worldId : inf.getInfluenceRelevantWorldIds()) {
			writeD(worldId);
			writeF(inf.getInfluence(worldId, SiegeRace.ELYOS));
			writeF(inf.getInfluence(worldId, SiegeRace.ASMODIANS));
			writeF(inf.getInfluence(worldId, SiegeRace.BALAUR));
		}
		writeH(fortresses.size());
		for (FortressLocation fortress : fortresses.values()) {
			writeD(fortress.getLocationId());
			writeC(fortress.getNextState());
		}
	}

}
