package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.AbstractPlayerInfoPacket.CHARNAME_MAX_LENGTH;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.model.AbyssRankingResult;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rhys2002, zdead, LokiReborn
 */
public class SM_ABYSS_RANKING_PLAYERS extends AionServerPacket {

	private List<AbyssRankingResult> data;
	private int lastUpdate;
	private int race;
	private int page;
	private boolean isEndPacket;

	public SM_ABYSS_RANKING_PLAYERS(int lastUpdate, List<AbyssRankingResult> data, Race race, int page, boolean isEndPacket) {
		this.lastUpdate = lastUpdate;
		this.data = data;
		this.race = race.getRaceId();
		this.page = page;
		this.isEndPacket = isEndPacket;
	}

	public SM_ABYSS_RANKING_PLAYERS(int lastUpdate, Race race) {
		this.lastUpdate = lastUpdate;
		this.data = Collections.emptyList();
		this.race = race.getRaceId();
		this.page = 0;
		this.isEndPacket = false;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(race);// 0:Elyos 1:Asmo
		writeD(lastUpdate);// Date
		writeD(page);
		writeD(isEndPacket ? 0x7F : 0);// 0:Nothing 1:Update Table
		writeH(data.size());// list size

		for (AbyssRankingResult rs : data) {
			writeD(rs.getRankPos());// Current Rank
			writeD(rs.getPlayerAbyssRank());// Abyss rank
			writeD((rs.getOldRankPos() == 0) ? 501 : rs.getOldRankPos());// Old Rank
			writeD(rs.getPlayerId()); // PlayerID
			writeD(race);
			writeD(rs.getPlayerClass().getClassId());// Class Id
			writeC(rs.getGender().getGenderId()); // Sex
			writeC(0); // unk
			writeC(0); // unk
			writeC(0); // unk
			writeQ(rs.getPlayerAP());// Abyss Points
			writeD(rs.getPlayerGP());// Glory Points
			writeH(rs.getPlayerLevel());
			writeS(rs.getPlayerName(), CHARNAME_MAX_LENGTH);// Two strings actually: player name + server name suffix (eg., SL for FastTrack)
			writeS(rs.getLegionName(), 42);// Legion Name
		}
	}
}
