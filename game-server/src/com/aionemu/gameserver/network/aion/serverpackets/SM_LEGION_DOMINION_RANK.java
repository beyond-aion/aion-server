package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.legionDominion.LegionDominionParticipantInfo;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Yeats
 */
public class SM_LEGION_DOMINION_RANK extends AionServerPacket {

	private final LegionDominionLocation loc;
	private final int rank;
	private final List<LegionDominionParticipantInfo> topParticipants;

	public SM_LEGION_DOMINION_RANK(LegionDominionLocation loc, Legion legion) {
		this.loc = loc;
		List<LegionDominionParticipantInfo> ranking = loc.getLegionRanking(false);
		LegionDominionParticipantInfo participant = legion == null ? null : loc.getParticipantInfo(legion.getLegionId());
		rank = participant == null ? 0 : ranking.indexOf(participant) + 1;
		topParticipants = ranking.size() > 25 ? ranking.subList(0,  25) : ranking;
		if (rank > topParticipants.size()) // if the ranked legion is not top-ranked, the last entry must be the ranked one
			topParticipants.set(topParticipants.size() - 1, ranking.get(rank - 1));
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(loc.getLocationId());
		writeC(rank);
		writeH(topParticipants.size());
		for (LegionDominionParticipantInfo participant : topParticipants) {
			writeD(participant.getPoints());
			writeD(participant.getTime());
			writeQ(participant.getDate());
			writeS(participant.getLegionName());
		}
	}
}
