package com.aionemu.gameserver.network.aion.serverpackets;


import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Nemiroff Date: 25.01.2010
 */
public class SM_ABYSS_RANK extends AionServerPacket {

	private AbyssRank rank;
	private int currentRankId;

	public SM_ABYSS_RANK(AbyssRank rank) {
		this.rank = rank;
		this.currentRankId = rank.getRank().getId();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeQ(rank.getAp()); // curAP
		writeD(rank.getCurrentGP());// curGP
		writeD(currentRankId); // curRank
		writeD(rank.getTopRanking()); // curRating
                
                writeD(0); // exp % removed with 4.5?
		writeD(rank.getAllKill()); // allKill
		writeD(rank.getMaxRank()); // maxRank

		writeD(rank.getDailyKill()); // dayKill
		writeQ(rank.getDailyAP()); // dayAP
		writeD(rank.getDailyGP()); // dayGP

		writeD(rank.getWeeklyKill()); // weekKill
		writeQ(rank.getWeeklyAP()); // weekAP
		writeD(rank.getWeeklyGP()); // week GP

		writeD(rank.getLastKill()); // laterKill
		writeQ(rank.getLastAP()); // laterAP
		writeD(rank.getLastGP());

		writeC(0x00); // unk
	}
}
