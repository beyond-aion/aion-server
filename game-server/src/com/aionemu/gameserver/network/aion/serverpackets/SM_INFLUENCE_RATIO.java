package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.SiegeService;

/**
 * @author Nemiroff
 */
public class SM_INFLUENCE_RATIO extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		Influence inf = Influence.getInstance();

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
	}
}
