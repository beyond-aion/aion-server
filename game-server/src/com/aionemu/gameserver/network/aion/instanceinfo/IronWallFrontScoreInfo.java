package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancereward.IronWallFrontReward;
import com.aionemu.gameserver.model.instance.playerreward.IronWallFrontPlayerReward;

/**
 * @author xTz
 */
public class IronWallFrontScoreInfo extends InstanceScoreInfo {

	private final IronWallFrontReward ironWallFrontReward;
	private final int type;
	private final int objectId;
	private final InstanceProgressionType instanceScoreType;

	public IronWallFrontScoreInfo(IronWallFrontReward ironWallFrontReward, int type, int objectId) {
		this.ironWallFrontReward = ironWallFrontReward;
		this.type = type;
		this.objectId = objectId;
		this.instanceScoreType = ironWallFrontReward.getInstanceProgressionType();
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeC(buf, type);

		IronWallFrontPlayerReward ironWallFrontPlayerReward = null;
		switch (type) {
			case 3:
				ironWallFrontPlayerReward = ironWallFrontReward.getPlayerReward(objectId);
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, ironWallFrontPlayerReward.getOwnerId()); // objectId
				writeD(buf, ironWallFrontPlayerReward.getRace().equals(Race.ELYOS) ? 0 : 1); // elyos 0 asmodians 1
				break;
			case 5: // reward
				ironWallFrontPlayerReward = ironWallFrontReward.getPlayerReward(objectId);
				writeD(buf, 100); // partitipation
				writeD(buf, ironWallFrontPlayerReward.getBaseReward());
				writeD(buf, ironWallFrontPlayerReward.getBonusReward());
				writeD(buf, ironWallFrontPlayerReward.getGloryPoints());
				writeD(buf, 0);
				if (ironWallFrontPlayerReward.getFragmentedCeramium() > 0) { // item one
					writeD(buf, 186000243);
					writeD(buf, ironWallFrontPlayerReward.getFragmentedCeramium());
				} else {
					writeD(buf, 0);
					writeD(buf, 0);
				}
				writeD(buf, 0);

				if (ironWallFrontPlayerReward.getIronWarFrontBox() > 0) { // item two
					writeD(buf, 188052729);
					writeD(buf, ironWallFrontPlayerReward.getIronWarFrontBox());
				} else {
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
				for (IronWallFrontPlayerReward reward : ironWallFrontElyos) {
					if (reward != null) {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, reward.getOwnerId());
					} else {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, 0);
					}
				}
				IronWallFrontPlayerReward[] ironWallFrontAsmodians = ironWallFrontReward.getPlayersByRace(Race.ASMODIANS);
				for (IronWallFrontPlayerReward reward : ironWallFrontAsmodians) {
					if (reward != null) {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, reward.getOwnerId());
					} else {
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
				writeD(buf, instanceScoreType.isReinforcing() ? 1 : 0xFFFF);// 1 | 65535 - 0xFFFF | 0
				// asmodians reward
				writeC(buf, 0);
				writeD(buf, ironWallFrontReward.getAsmodiansKills().intValue());
				writeD(buf, ironWallFrontReward.getAsmodiansPoint().intValue());
				writeD(buf, 1); // elyos
				writeD(buf, instanceScoreType.isReinforcing() ? 1 : 0xFFFF); // 1 | 65535 - 0xFFFF | 0
				break;
			case 10:
				writeC(buf, 0);
				writeD(buf, ironWallFrontReward.getKillsByRace(objectId == 0 ? Race.ELYOS : Race.ASMODIANS).intValue());
				writeD(buf, ironWallFrontReward.getPointsByRace(objectId == 0 ? Race.ELYOS : Race.ASMODIANS).intValue());
				writeD(buf, objectId); // elyos 0 asmodians 1
				writeD(buf, 0);
				break;
		}
	}

}
