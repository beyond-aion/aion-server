package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.instancereward.IronWallFrontReward;
import com.aionemu.gameserver.model.instance.playerreward.IronWallFrontPlayerReward;

/**
 * @author xTz
 */
public class IronWallFrontScoreInfo extends InstanceScoreInfo<IronWallFrontReward> {

	private final int type;
	private final int objectId;

	public IronWallFrontScoreInfo(IronWallFrontReward reward, int type, int objectId) {
		super(reward);
		this.type = type;
		this.objectId = objectId;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeC(buf, type);

		IronWallFrontPlayerReward ironWallFrontPlayerReward;
		switch (type) {
			case 3:
				ironWallFrontPlayerReward = reward.getPlayerReward(objectId);
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, ironWallFrontPlayerReward.getOwnerId()); // objectId
				writeD(buf, ironWallFrontPlayerReward.getRace() == Race.ELYOS ? 0 : 1);
				break;
			case 5: // reward
				ironWallFrontPlayerReward = reward.getPlayerReward(objectId);
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
				IronWallFrontPlayerReward[] ironWallFrontElyos = reward.getPlayersByRace(Race.ELYOS);
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
				IronWallFrontPlayerReward[] ironWallFrontAsmodians = reward.getPlayersByRace(Race.ASMODIANS);
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
				writeD(buf, reward.getElyosKills());
				writeD(buf, reward.getElyosPoints());
				writeD(buf, 0); // asmodians 0
				writeD(buf, reward.getInstanceProgressionType().isReinforcing() ? 1 : 0xFFFF);// 1 | 65535 - 0xFFFF | 0
				// asmodians reward
				writeC(buf, 0);
				writeD(buf, reward.getAsmodiansKills());
				writeD(buf, reward.getAsmodiansPoints());
				writeD(buf, 1); // elyos
				writeD(buf, reward.getInstanceProgressionType().isReinforcing() ? 1 : 0xFFFF); // 1 | 65535 - 0xFFFF | 0
				break;
			case 10:
				writeC(buf, 0);
				writeD(buf, objectId == 0 ? reward.getElyosKills() : reward.getAsmodiansKills());
				writeD(buf, objectId == 0 ? reward.getElyosPoints() : reward.getAsmodiansPoints());
				writeD(buf, objectId); // elyos 0 asmodians 1
				writeD(buf, 0);
				break;
		}
	}

}
