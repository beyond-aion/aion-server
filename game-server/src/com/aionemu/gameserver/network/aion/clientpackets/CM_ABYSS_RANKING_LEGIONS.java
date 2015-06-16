package com.aionemu.gameserver.network.aion.clientpackets;

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

	private Race queriedRace;
	private AbyssRankUpdateType updateType;
	private int raceId;

	private static final Logger log = LoggerFactory.getLogger(CM_ABYSS_RANKING_LEGIONS.class);
	public CM_ABYSS_RANKING_LEGIONS(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		raceId = readC();
		switch (raceId) {
			case 0:
				queriedRace = Race.ELYOS;
				updateType = AbyssRankUpdateType.LEGION_ELYOS;
				break;
			case 1:
				queriedRace = Race.ASMODIANS;
				updateType = AbyssRankUpdateType.LEGION_ASMODIANS;
				break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		// calculate rankings and send packet
		if (queriedRace != null) {
			Player player = this.getConnection().getActivePlayer();
			if (player.isAbyssRankListUpdated(updateType)){
				sendPacket(new SM_ABYSS_RANKING_LEGIONS(AbyssRankingCache.getInstance().getLastUpdate(), queriedRace));
			}
			else {
				SM_ABYSS_RANKING_LEGIONS results = AbyssRankingCache.getInstance().getLegions(queriedRace);
				sendPacket(results);
				player.setAbyssRankListUpdated(updateType);
			}
		}
		else {
			log.warn("Received invalid raceId: " + raceId);
		}
	}
}
