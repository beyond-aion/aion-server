package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.PvPArenaReward;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;

/**
 *
 * @author xTz
 */
public class ArenaOfGloryScoreInfo extends InstanceScoreInfo {

    private final PvPArenaReward arenaReward;
    private final Integer ownerObject;
    private final List<Player> players;

    public ArenaOfGloryScoreInfo(PvPArenaReward arenaReward, Integer ownerObject, List<Player> players) {
        this.arenaReward = arenaReward;
        this.ownerObject = ownerObject;
        this.players = players;
    }

    @Override
    public void writeMe(ByteBuffer buf) {
 
        PvPArenaPlayerReward rewardedPlayer = arenaReward.getPlayerReward(ownerObject);
        int rank, points, playerCount = 0;
        boolean isRewarded = arenaReward.isRewarded();
        for (Player player : players) {
            InstancePlayerReward reward = arenaReward.getPlayerReward(player.getObjectId());
            PvPArenaPlayerReward playerReward = (PvPArenaPlayerReward) reward;
            points = playerReward.getPoints();
            rank = arenaReward.getRank(playerReward.getScorePoints());
            writeD(buf, playerReward.getOwner()); // obj
            writeD(buf, playerReward.getPvPKills()); // kills
            writeD(buf, isRewarded ? points + playerReward.getTimeBonus() : points); // points
            writeD(buf, 0); // unk
            writeC(buf, 0); // unk
            writeC(buf, player.getPlayerClass().getClassId()); // class id
            writeC(buf, 1); // unk
            writeC(buf, rank); // top position
            writeD(buf, playerReward.getRemaningTime()); // instance buff time
            writeD(buf, isRewarded ? playerReward.getTimeBonus() : 0); // time bonus
            writeD(buf, 0); // unk
            writeD(buf, 0); // unk
            writeH(buf, isRewarded ? (short) (playerReward.getParticipation() * 100) : 0); // participation
            writeS(buf, player.getName(), 54); // playerName
            playerCount++;
        }
        if (playerCount < 12) {
            writeB(buf, new byte[92 * (12 - playerCount)]); // spaces
        }
        if (isRewarded && arenaReward.canRewarded() && rewardedPlayer != null) {
            writeD(buf, rewardedPlayer.getBasicAP()); // basicRewardAp
            writeD(buf, rewardedPlayer.getScoreAP()); // scoreRewardAp
            writeD(buf, rewardedPlayer.getRankingAP()); // rankingRewardAp
            writeD(buf, rewardedPlayer.getBasicGP()); // basicRewardGp
            writeD(buf, rewardedPlayer.getScoreGP()); // scoreRewardGp
            writeD(buf, rewardedPlayer.getRankingGP()); // rankingRewardGp
            writeB(buf, new byte[32]);
            if (rewardedPlayer.getCeramiumMedal() != 0) {
                writeD(buf, 186000242); // 186000242
                writeD(buf, rewardedPlayer.getCeramiumMedal()); // mithril medal
            }
            else if (rewardedPlayer.getMithrilMedal() != 0) {
                writeD(buf, 186000147); // 186000147
                writeD(buf, rewardedPlayer.getMithrilMedal()); // mithril medal
            }
            else if (rewardedPlayer.getPlatinumMedal() != 0) {
                writeD(buf, 186000096); // 186000096
                writeD(buf, rewardedPlayer.getPlatinumMedal()); // platinum medal
            }
            else if (rewardedPlayer.getLifeSerum() != 0) {
                writeD(buf, 162000077); // 162000077
                writeD(buf, rewardedPlayer.getLifeSerum()); // life serum
            }
            else {
                writeD(buf, 0);
                writeD(buf, 0);
            }
            if (rewardedPlayer.getGloriousInsignia() != 0) {
                writeD(buf, 182213259); // 182213259
                writeD(buf, rewardedPlayer.getGloriousInsignia()); // glorious insignia
            }
            else {
                writeD(buf, 0);
                writeD(buf, 0);
            }
        }
        else {
            writeB(buf, new byte[60]);
        }
        writeD(buf, arenaReward.getBuffId()); // instance buff id
        writeD(buf, 0); // unk
        writeD(buf, arenaReward.getRound()); // round
        writeD(buf, arenaReward.getCapPoints()); // cap points
        writeD(buf, 3); // possible rounds
        writeD(buf, 0); // unk
    }

}
