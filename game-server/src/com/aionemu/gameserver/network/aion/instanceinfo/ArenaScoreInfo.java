package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.PvPArenaReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;

/**
 * @author Neon
 */
public abstract class ArenaScoreInfo extends InstanceScoreInfo {

	protected final PvPArenaReward arenaReward;
	private final List<Player> players;

	protected ArenaScoreInfo(PvPArenaReward arenaReward, List<Player> players) {
		this.arenaReward = arenaReward;
		this.players = players;
	}

	protected void writePlayerRewards(ByteBuffer buf) {
		int playerCount = 0;
		for (Player player : players) {
			PvPArenaPlayerReward playerReward = arenaReward.getPlayerReward(player.getObjectId());
			if (playerReward == null)
				continue;
			writeD(buf, playerReward.getOwnerId());
			writeD(buf, playerReward.getPvPKills());
			writeD(buf, arenaReward.isRewarded() ? playerReward.getPoints() + playerReward.getTimeBonus() : playerReward.getPoints());
			writeD(buf, 0); // unk
			writeC(buf, 0); // unk
			writeC(buf, player.getPlayerClass().getClassId());
			writeC(buf, 1); // unk
			writeC(buf, arenaReward.getRank(playerReward.getScorePoints())); // top position
			writeD(buf, playerReward.getRemaningTime()); // instance buff time
			writeD(buf, arenaReward.isRewarded() ? playerReward.getTimeBonus() : 0);
			writeD(buf, 0); // unk
			writeD(buf, 0); // unk
			writeH(buf, arenaReward.isRewarded() ? (short) (playerReward.getParticipation() * 100) : 0); // participation
			writeS(buf, player.getName(), 54);
			playerCount++;
		}
		if (playerCount < 12)
			writeB(buf, new byte[92 * (12 - playerCount)]); // spaces
	}

	protected void writeAP(ByteBuffer buf, PvPArenaPlayerReward playerReward) {
		writeD(buf, playerReward.getBasicAP());
		writeD(buf, playerReward.getScoreAP());
		writeD(buf, playerReward.getRankingAP());
	}

	protected void writeGP(ByteBuffer buf, PvPArenaPlayerReward playerReward) {
		writeD(buf, playerReward.getBasicGP());
		writeD(buf, playerReward.getScoreGP());
		writeD(buf, playerReward.getRankingGP());
	}
}
