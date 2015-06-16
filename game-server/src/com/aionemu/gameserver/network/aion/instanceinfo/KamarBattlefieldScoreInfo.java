package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.KamarReward;
import com.aionemu.gameserver.model.instance.playerreward.KamarPlayerReward;
import java.nio.ByteBuffer;

/**
 *
 * @author xTz
 */
public class KamarBattlefieldScoreInfo extends InstanceScoreInfo {

    private final KamarReward kamarReward;
    private final int type;
    private final Integer object;
    private final InstanceScoreType instanceScoreType;

    public KamarBattlefieldScoreInfo(KamarReward kamarReward, int type, Integer object) {
        this.kamarReward = kamarReward;
        this.type = type;
        this.object = object;
        this.instanceScoreType = kamarReward.getInstanceScoreType();
    }

    @Override
    public void writeMe(ByteBuffer buf) {
        writeC(buf, type);
				
                KamarPlayerReward kamarPlayerReward = null;
				switch (type) {
					case 3:
						kamarPlayerReward = kamarReward.getPlayerReward(object);
                        writeD(buf, 10); // buff id
						writeD(buf, kamarPlayerReward.getRemaningTime()); // bufftime
						writeD(buf, kamarPlayerReward.getOwner()); // objectId
						writeD(buf, kamarPlayerReward.getRace().equals(Race.ELYOS) ? 0 : 1); // elyos 0 asmodians 1 
						break;
					case 4:
						kamarPlayerReward = kamarReward.getPlayerReward(object);
                        writeD(buf, 10); // buff id
						writeD(buf, kamarPlayerReward.getRemaningTime()); // bufftime
						writeD(buf, kamarPlayerReward.getOwner()); // objectId
						break;
					case 5: // reward
						kamarPlayerReward = kamarReward.getPlayerReward(object);
						writeD(buf, 100); // partitipation
						writeD(buf, kamarPlayerReward.getBaseReward());
						writeD(buf, kamarPlayerReward.getBonusReward());
						writeD(buf, kamarPlayerReward.getGloryPoints());
						writeD(buf, 0);
						if (kamarPlayerReward.getKamarBox() > 0) {
							writeD(buf, 188052670);
							writeD(buf, kamarPlayerReward.getKamarBox());
						}
						else {
							writeD(buf, 0);
							writeD(buf, 0);
						}
						writeD(buf, 0);
						if (kamarPlayerReward.getBloodMarks()> 0) {
							writeD(buf, 1860000236);
							writeD(buf, kamarPlayerReward.getBloodMarks());
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
						break;
					case 6:
						writeD(buf, 100);
						KamarPlayerReward[] kamarElyos = kamarReward.getPlayersByRace(Race.ELYOS);
						for (int i = 0; i < kamarElyos.length; i++) {
							KamarPlayerReward reward = kamarElyos[i];
							if (reward != null) {
								writeD(buf, 10); // buff id
								writeD(buf, reward.getRemaningTime()); // bufftime
								writeD(buf, reward.getOwner());
							}
							else {
								writeD(buf, 0);
								writeD(buf, 0);
								writeD(buf, 0);
							}
						}
						KamarPlayerReward[] kamarAsmodians = kamarReward.getPlayersByRace(Race.ASMODIANS);
						for (int i = 0; i < kamarAsmodians.length; i++) {
							KamarPlayerReward reward = kamarAsmodians[i];
							if (reward != null) {
								writeD(buf, 10); // buff id
								writeD(buf, reward.getRemaningTime()); // bufftime
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
						writeD(buf, kamarReward.getElyosKills().intValue());
						writeD(buf, kamarReward.getElyosPoints().intValue());
						writeD(buf, 0); // asmodians 0
						writeD(buf, instanceScoreType.isReinforsing() ? 1 : 0xFFFF);// 1 | 65535 - 0xFFFF | 0
						// asmodians reward
						writeC(buf, 0);
						writeD(buf, kamarReward.getAsmodiansKills().intValue());
						writeD(buf, kamarReward.getAsmodiansPoint().intValue());
						writeD(buf, 1); // elyos
						writeD(buf, instanceScoreType.isReinforsing() ? 1 : 0xFFFF); // 1 | 65535 - 0xFFFF | 0
						break;
					case 10:
						writeC(buf, 0);
						writeD(buf, kamarReward.getKillsByRace(object == 0 ? Race.ELYOS :Race.ASMODIANS ).intValue());
						writeD(buf, kamarReward.getPointsByRace(object == 0 ? Race.ELYOS :Race.ASMODIANS ).intValue());
						writeD(buf, object); // elyos 0 asmodians 1
						writeD(buf, 0);
						break;
				}
    }
    
}
