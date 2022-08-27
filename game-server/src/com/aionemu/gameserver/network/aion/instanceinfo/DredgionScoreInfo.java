package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.DredgionReward;
import com.aionemu.gameserver.model.instance.playerreward.DredgionPlayerReward;

/**
 * @author xTz
 */
public class DredgionScoreInfo extends InstanceScoreInfo<DredgionReward> {

	private final List<Player> players;

	public DredgionScoreInfo(DredgionReward reward, List<Player> players) {
		super(reward);
		this.players = players;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		fillTableWithGroup(buf, Race.ELYOS);
		fillTableWithGroup(buf, Race.ASMODIANS);
		int elyosScore = reward.getElyosPoints();
		int asmosScore = reward.getAsmodiansPoints();
		writeD(buf, reward.getInstanceProgressionType().isEndProgress() ? (asmosScore > elyosScore ? 1 : 0) : 255);
		writeD(buf, elyosScore);
		writeD(buf, asmosScore);
		for (DredgionReward.DredgionRooms dredgionRoom : reward.getDredgionRooms()) {
			writeC(buf, dredgionRoom.getState());
		}
	}

	private void fillTableWithGroup(ByteBuffer buf, Race race) {
		int count = 0;
		for (Player player : players) {
			if (race != player.getRace()) {
				continue;
			}
			DredgionPlayerReward dpr = reward.getPlayerReward(player.getObjectId());
			writeD(buf, dpr.getOwnerId()); // playerObjectId
			writeD(buf, player.getAbyssRank().getRank().getId()); // playerRank
			writeD(buf, dpr.getPvPKills()); // pvpKills
			writeD(buf, dpr.getMonsterKills()); // monsterKills
			writeD(buf, dpr.getZoneCaptured()); // captured
			writeD(buf, dpr.getPoints()); // playerScore

			if (reward.getInstanceProgressionType().isEndProgress()) {
				boolean winner = race == reward.getRaceWithHighestPoints();
				writeD(buf, (winner ? reward.getWinnerApReward() : reward.getLoserApReward()) + (int) (dpr.getPoints() * 1.6f)); // apBonus1
				writeD(buf, (winner ? reward.getWinnerApReward() : reward.getLoserApReward())); // apBonus2
			} else {
				writeB(buf, new byte[8]);
			}

			writeC(buf, player.getPlayerClass().getClassId()); // playerClass
			writeC(buf, 0); // unk
			writeS(buf, player.getName(), 54); // playerName
			count++;
		}
		if (count < 6) {
			writeB(buf, new byte[88 * (6 - count)]); // spaces
		}
	}

}
