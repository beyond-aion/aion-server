package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.abyss.AbyssRankingCache;

/**
 * @author Nemiroff Date: 25.01.2010
 */
public class SM_ABYSS_RANK extends AionServerPacket {

	private final AbyssRank rank;
	private final int rankingListPosition;

	public SM_ABYSS_RANK(Player player) {
		this(player, null);
	}

	public SM_ABYSS_RANK(Player player, Integer rankingListPosition) {
		this.rank = player.getAbyssRank();
		this.rankingListPosition = rankingListPosition == null ? AbyssRankingCache.getInstance().getRankingListPosition(player) : rankingListPosition;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeQ(rank.getAp());
		writeD(rank.getCurrentGP());
		writeD(rank.getRank().getId());
		writeD(rankingListPosition);

		writeD(0); // exp % removed with 4.5?
		writeD(rank.getAllKill());
		writeD(rank.getMaxRank());

		writeD(rank.getDailyKill());
		writeQ(rank.getDailyAP());
		writeD(rank.getDailyGP());

		writeD(rank.getWeeklyKill());
		writeQ(rank.getWeeklyAP());
		writeD(rank.getWeeklyGP());

		writeD(rank.getLastKill());
		writeQ(rank.getLastAP());
		writeD(rank.getLastGP());

		writeC(0x00); // unk
	}
}
