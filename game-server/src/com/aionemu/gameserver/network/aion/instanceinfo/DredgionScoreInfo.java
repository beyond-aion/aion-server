package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.DredgionReward;
import com.aionemu.gameserver.model.instance.playerreward.DredgionPlayerReward;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

/**
 *
 * @author xTz
 */
public class DredgionScoreInfo extends InstanceScoreInfo{
    private final DredgionReward dredgionReward;
    private final List<Player> players;
    private final InstanceScoreType instanceScoreType;
    public DredgionScoreInfo(DredgionReward dredgionReward, List<Player> players) {
        this.dredgionReward = dredgionReward;
        this.players = players;
        this.instanceScoreType = dredgionReward.getInstanceScoreType();
    }

    @Override
    public void writeMe(ByteBuffer buf) {
        fillTableWithGroup(buf, Race.ELYOS);
        fillTableWithGroup(buf, Race.ASMODIANS);
        int elyosScore = dredgionReward.getPointsByRace(Race.ELYOS).intValue();
        int asmosScore = dredgionReward.getPointsByRace(Race.ASMODIANS).intValue();
        writeD(buf, instanceScoreType.isEndProgress() ? (asmosScore > elyosScore ? 1 : 0) : 255);
        writeD(buf, elyosScore);
        writeD(buf, asmosScore);
        writeH(buf, 0); // [3.5]
        for (DredgionReward.DredgionRooms dredgionRoom : dredgionReward.getDredgionRooms()) {
            writeC(buf, dredgionRoom.getState());
        }
    }
    private void fillTableWithGroup(ByteBuffer buf, Race race) {
		int count = 0;
		for (Player player : players) {
			if (!race.equals(player.getRace())) {
				continue;
			}
			InstancePlayerReward playerReward = dredgionReward.getPlayerReward(player.getObjectId());
			DredgionPlayerReward dpr = (DredgionPlayerReward) playerReward;
			writeD(buf, playerReward.getOwner()); // playerObjectId
			writeD(buf, player.getAbyssRank().getRank().getId()); // playerRank
			writeD(buf, dpr.getPvPKills()); // pvpKills
			writeD(buf, dpr.getMonsterKills()); // monsterKills
			writeD(buf, dpr.getZoneCaptured()); // captured
			writeD(buf, dpr.getPoints()); // playerScore

			if (instanceScoreType.isEndProgress()) {
				boolean winner = race.equals(dredgionReward.getWinningRace());
				writeD(buf, (winner ? dredgionReward.getWinnerPoints() : dredgionReward.getLooserPoints()) + (int) (dpr.getPoints() * 1.6f)); // apBonus1
				writeD(buf, (winner ? dredgionReward.getWinnerPoints() : dredgionReward.getLooserPoints())); // apBonus2
			}
			else {
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
