package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.AbstractPlayerInfoPacket.CHARNAME_MAX_LENGTH;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.dao.AbyssRankDAO.RankingListPlayer;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rhys2002, zdead, LokiReborn
 */
public class SM_ABYSS_RANKING_PLAYERS extends AionServerPacket {

	private final List<RankingListPlayer> players;
	private final int lastUpdate;
	private final int race;
	private final int page;
	private final boolean isEndPacket;

	public SM_ABYSS_RANKING_PLAYERS(int lastUpdate, Race race) {
		this(lastUpdate, Collections.emptyList(), race, 0, false);
	}

	public SM_ABYSS_RANKING_PLAYERS(int lastUpdate, List<RankingListPlayer> players, Race race, int page, boolean isEndPacket) {
		this.lastUpdate = lastUpdate;
		this.players = players;
		this.race = race.getRaceId();
		this.page = page;
		this.isEndPacket = isEndPacket;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(race);
		writeD(lastUpdate);
		writeD(page);
		writeD(isEndPacket ? 0x7F : 0);// 0:Nothing 1:Update Table
		writeH(players.size());

		for (RankingListPlayer player : players) {
			writeD(player.position());
			writeD(player.abyssRank());
			writeD(player.oldPosition());
			writeD(player.id());
			writeD(race);
			writeD(player.playerClass().getClassId());
			writeC(player.gender().getGenderId());
			writeC(0); // unk
			writeC(0); // unk
			writeC(0); // unk
			writeQ(player.ap());
			writeD(player.gp());
			writeH(player.level());
			writeS(player.name(), CHARNAME_MAX_LENGTH);// Two strings actually: player name + server name suffix (eg., SL for FastTrack)
			writeS(player.legionName(), 42);
		}
	}
}
