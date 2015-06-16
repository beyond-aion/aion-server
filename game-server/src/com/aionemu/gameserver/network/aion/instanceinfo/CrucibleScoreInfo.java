package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.playerreward.CruciblePlayerReward;
import java.nio.ByteBuffer;
import javolution.util.FastList;

/**
 *
 * @author xTz
 */
public class CrucibleScoreInfo extends InstanceScoreInfo {

    private final InstanceScoreType instanceScoreType;
    private final InstanceReward instanceReward;

    public CrucibleScoreInfo(InstanceReward instanceReward) {
        this.instanceScoreType = instanceReward.getInstanceScoreType();
        this.instanceReward = instanceReward;
    }

    public CrucibleScoreInfo(InstanceReward instanceReward, InstanceScoreType instanceScoreType) {
        this.instanceScoreType = instanceScoreType;
        this.instanceReward = instanceReward;
    }

    @Override
    public void writeMe(ByteBuffer buf) {
        int playerCount = 0;
        for (CruciblePlayerReward playerReward : (FastList<CruciblePlayerReward>) instanceReward.getInstanceRewards()) {
            writeD(buf, playerReward.getOwner()); // obj
            writeD(buf, playerReward.getPoints()); // points
            writeD(buf, instanceScoreType.isEndProgress() ? 3 : 1);
            writeD(buf, playerReward.getInsignia());
            playerCount++;
        }
        if (playerCount < 6) {
            writeB(buf, new byte[16 * (6 - playerCount)]); // spaces
        }
    }

}
