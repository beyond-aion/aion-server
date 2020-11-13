package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.dao.AbyssRankDAO.RankingListLegion;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author zdead, LokiReborn
 */
public class SM_ABYSS_RANKING_LEGIONS extends AionServerPacket {

	private final List<RankingListLegion> rankingList;
	private final Race race;
	private final int updateTime;
	private final boolean clearListBeforeUpdate;

	public SM_ABYSS_RANKING_LEGIONS(int updateTime, Race race) {
		this(updateTime, Collections.emptyList(), race, false);
	}

	public SM_ABYSS_RANKING_LEGIONS(int updateTime, List<RankingListLegion> rankingList, Race race) {
		this(updateTime, rankingList, race, true);
	}

	private SM_ABYSS_RANKING_LEGIONS(int updateTime, List<RankingListLegion> rankingList, Race race, boolean clearListBeforeUpdate) {
		this.updateTime = updateTime;
		this.rankingList = rankingList;
		this.race = race;
		this.clearListBeforeUpdate = clearListBeforeUpdate;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(race.getRaceId());
		writeD(updateTime);
		writeD(clearListBeforeUpdate ? 1 : 0);
		writeD(clearListBeforeUpdate ? 1 : 0);
		writeH(rankingList.size());
		for (RankingListLegion legion : rankingList) {
			writeD(legion.position());
			writeD(legion.oldPosition());
			writeD(legion.id());
			writeD(race.getRaceId());
			writeC(legion.level());
			writeD(legion.memberCount());
			writeQ(legion.contributionPoints());
			writeS(legion.name(), 40);
		}
	}
}
