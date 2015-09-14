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
public class ChaosScoreInfo extends InstanceScoreInfo{
    private final PvPArenaReward arenaReward;
    private final Integer ownerObject;
    private final List<Player> players;

    public ChaosScoreInfo(PvPArenaReward arenaReward, Integer ownerObject, List<Player> players) {
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
            
            writeD(buf,186000130); // 186000130
            writeD(buf,rewardedPlayer.getBasicCrucible()); // basicRewardCrucibleIn
            writeD(buf,rewardedPlayer.getScoreCrucible()); // scoreRewardCrucibleIn
            writeD(buf,rewardedPlayer.getRankingCrucible()); // rankingRewardCrucibleIn
            writeD(buf,186000137); // 186000137
            writeD(buf,rewardedPlayer.getBasicCourage()); // basicRewardCourageIn
            writeD(buf,rewardedPlayer.getScoreCourage()); // scoreRewardCourageIn
            writeD(buf,rewardedPlayer.getRankingCourage()); // rankingRewardCourageIn
            if (rewardedPlayer.getOpportunity() != 0) {
                writeD(buf,186000165); // 186000165
                writeD(buf,rewardedPlayer.getOpportunity()); // opportunity token
            }
            else if (rewardedPlayer.getGloryTicket() != 0) {
                writeD(buf,186000185); // 186000185
                writeD(buf,rewardedPlayer.getGloryTicket()); // glory ticket
            }
            else {
                writeD(buf,0);
                writeD(buf,0);
            }
            writeD(buf,0);
            writeD(buf,0);

        }
        else {
            writeB(buf,new byte[60]);
        }
        writeD(buf,arenaReward.getBuffId()); // instance buff id
        writeD(buf,0); // unk
        writeD(buf,arenaReward.getRound()); // round
        writeD(buf,arenaReward.getCapPoints()); // cap points
        writeD(buf,3); // possible rounds
        writeD(buf, 0); // posssible reward table
    }
    
}
