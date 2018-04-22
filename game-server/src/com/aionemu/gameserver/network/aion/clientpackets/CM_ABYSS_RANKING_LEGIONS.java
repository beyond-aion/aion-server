package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank.AbyssRankUpdateType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANKING_LEGIONS;
import com.aionemu.gameserver.services.abyss.AbyssRankingCache;

/**
 * @author SheppeR
 */
public class CM_ABYSS_RANKING_LEGIONS extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_ABYSS_RANKING_LEGIONS.class);

	private byte raceId;

	public CM_ABYSS_RANKING_LEGIONS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		raceId = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Race queriedRace;
		AbyssRankUpdateType updateType;
		switch (raceId) {
			case 0:
				queriedRace = Race.ELYOS;
				updateType = AbyssRankUpdateType.LEGION_ELYOS;
				break;
			case 1:
				queriedRace = Race.ASMODIANS;
				updateType = AbyssRankUpdateType.LEGION_ASMODIANS;
				break;
			default:
				log.warn("Received invalid raceId (" + raceId + ") from player " + player);
				return;
		}
		// calculate rankings and send packet
		SM_ABYSS_RANKING_LEGIONS legionRanking;
		if (player.isAbyssRankListUpdated(updateType)) {
			legionRanking = new SM_ABYSS_RANKING_LEGIONS(AbyssRankingCache.getInstance().getLastUpdate(), queriedRace);
		} else {
			legionRanking = AbyssRankingCache.getInstance().getLegions(queriedRace);
			player.setAbyssRankListUpdated(updateType);
		}
		sendPacket(legionRanking);
	}
}
