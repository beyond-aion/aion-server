package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.IronWallFrontReward;
import com.aionemu.gameserver.model.instance.playerreward.IronWallFrontPlayerReward;
import java.nio.ByteBuffer;

/**
 *
 * @author xTz
 */
public class IronWallFrontScoreInfo extends InstanceScoreInfo {

    private final IronWallFrontReward ironWallFrontReward;
    private final int type;
    private final Integer object;
    private final InstanceScoreType instanceScoreType;

    public IronWallFrontScoreInfo(IronWallFrontReward ironWallFrontReward, int type, Integer object) {
        this.ironWallFrontReward = ironWallFrontReward;
        this.type = type;
        this.object = object;
        this.instanceScoreType = ironWallFrontReward.getInstanceScoreType();
    }

    @Override
    public void writeMe(ByteBuffer buf) {
        writeC(buf, type);
				
        IronWallFrontPlayerReward ironWallFrontPlayerReward = null;
        switch (type) {
            case 3:
                ironWallFrontPlayerReward = ironWallFrontReward.getPlayerReward(object);
                writeD(buf, 0);
                writeD(buf, 0);
                writeD(buf, ironWallFrontPlayerReward.getOwner()); // objectId
                writeD(buf, ironWallFrontPlayerReward.getRace().equals(Race.ELYOS) ? 0 : 1); // elyos 0 asmodians 1 
                break;
            case 5: // reward
                ironWallFrontPlayerReward = ironWallFrontReward.getPlayerReward(object);
                writeD(buf, 100); // partitipation
                writeD(buf, ironWallFrontPlayerReward.getBaseReward());
                writeD(buf, ironWallFrontPlayerReward.getBonusReward());
                writeD(buf, ironWallFrontPlayerReward.getGloryPoints());
                writeD(buf, 0);
                if (ironWallFrontPlayerReward.getFragmentedCeramium()> 0) { // item one
                    writeD(buf,186000243);
                    writeD(buf,ironWallFrontPlayerReward.getFragmentedCeramium());
                }
                else {
                    writeD(buf ,0);
                    writeD(buf, 0);
                }
                writeD(buf, 0);

                if (ironWallFrontPlayerReward.getIronWarFrontBox()> 0) { // item two
                    writeD(buf,188052729);
                    writeD(buf,ironWallFrontPlayerReward.getIronWarFrontBox());
                }
                else {
                    writeD(buf, 0);
                    writeD(buf, 0);
                }
                writeD(buf, 0);
                writeD(buf, 0);
                writeD(buf, 0);
                writeD(buf, 0);
                writeD(buf, 0);
                writeD(buf, 0);
                break;
            case 6:
                writeD(buf, 100);
                IronWallFrontPlayerReward[] ironWallFrontElyos = ironWallFrontReward.getPlayersByRace(Race.ELYOS);
                for (int i = 0; i < ironWallFrontElyos.length; i++) {
                    IronWallFrontPlayerReward reward = ironWallFrontElyos[i];
                    if (reward != null) {
                        writeD(buf, 0); 
                        writeD(buf, 0);
                        writeD(buf, reward.getOwner());
                    }
                    else {
                        writeD(buf, 0);
                        writeD(buf, 0);
                        writeD(buf, 0);
                    }
                }
                IronWallFrontPlayerReward[] ironWallFrontAsmodians = ironWallFrontReward.getPlayersByRace(Race.ASMODIANS);
                for (int i = 0; i < ironWallFrontAsmodians.length; i++) {
                    IronWallFrontPlayerReward reward = ironWallFrontAsmodians[i];
                    if (reward != null) {
                        writeD(buf, 0);
                        writeD(buf, 0);
                        writeD(buf, reward.getOwner());
                    }
                    else {
                        writeD(buf, 0);
                        writeD(buf, 0);
                        writeD(buf, 0);
                    }
                }
                // elyos reward
                writeC(buf, 0);
                writeD(buf, ironWallFrontReward.getElyosKills().intValue());
                writeD(buf, ironWallFrontReward.getElyosPoints().intValue());
                writeD(buf, 0); // asmodians 0
                writeD(buf, instanceScoreType.isReinforsing() ? 1 : 0xFFFF);// 1 | 65535 - 0xFFFF | 0
                // asmodians reward
                writeC(buf, 0);
                writeD(buf, ironWallFrontReward.getAsmodiansKills().intValue());
                writeD(buf, ironWallFrontReward.getAsmodiansPoint().intValue());
                writeD(buf, 1); // elyos
                writeD(buf, instanceScoreType.isReinforsing() ? 1 : 0xFFFF); // 1 | 65535 - 0xFFFF | 0
                break;
            case 10:
                writeC(buf, 0);
                writeD(buf, ironWallFrontReward.getKillsByRace(object == 0 ? Race.ELYOS :Race.ASMODIANS ).intValue());
                writeD(buf, ironWallFrontReward.getPointsByRace(object == 0 ? Race.ELYOS :Race.ASMODIANS ).intValue());
                writeD(buf, object); // elyos 0 asmodians 1
                writeD(buf, 0);
                break;
        }
    }
    
}
