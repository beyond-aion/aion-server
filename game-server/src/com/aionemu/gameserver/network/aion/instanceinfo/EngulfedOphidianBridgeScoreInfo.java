package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.instancereward.EngulfedOphidianBridgeReward;
import com.aionemu.gameserver.model.instance.playerreward.EngulfedOphidianBridgePlayerReward;

/**
 * @author xTz
 */
public class EngulfedOphidianBridgeScoreInfo extends InstanceScoreInfo<EngulfedOphidianBridgeReward> {

	private final int type;
	private final int objectId;

	public EngulfedOphidianBridgeScoreInfo(EngulfedOphidianBridgeReward reward, int type, int objectId) {
		super(reward);
		this.type = type;
		this.objectId = objectId;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeC(buf, type);
		EngulfedOphidianBridgePlayerReward engulfedOBPlayerReward;
		switch (type) {
			case 3:
				engulfedOBPlayerReward = reward.getPlayerReward(objectId);
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, engulfedOBPlayerReward.getOwnerId()); // objectId
				writeD(buf, engulfedOBPlayerReward.getRace() == Race.ELYOS ? 0 : 1);
				break;
			case 5: // reward
				engulfedOBPlayerReward = reward.getPlayerReward(objectId);
				writeD(buf, 100); // partitipation
				writeD(buf, engulfedOBPlayerReward.getBaseReward());
				writeD(buf, engulfedOBPlayerReward.getBonusReward());
				writeD(buf, engulfedOBPlayerReward.getGloryPoints());
				writeD(buf, 0);
				if (engulfedOBPlayerReward.getOphidianBox() > 0) {
					writeD(buf, 188052681);
					writeD(buf, engulfedOBPlayerReward.getOphidianBox());
				} else {
					writeD(buf, 0);
					writeD(buf, 0);
				}
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, 0);
				if (engulfedOBPlayerReward.getOBOpportunityBundle() > 0) {
					writeD(buf, 188053209);
					writeD(buf, engulfedOBPlayerReward.getOBOpportunityBundle());
				} else {
					writeD(buf, 0);
					writeD(buf, 0);
				}
				writeD(buf, 0);
				writeD(buf, 0);
				break;
			case 6:
				writeD(buf, 100);
				EngulfedOphidianBridgePlayerReward[] engulfedOBElyos = reward.getPlayersByRace(Race.ELYOS);
				for (EngulfedOphidianBridgePlayerReward elyosReward : engulfedOBElyos) {
					if (elyosReward != null) {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, elyosReward.getOwnerId());
					} else {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, 0);
					}
				}
				EngulfedOphidianBridgePlayerReward[] engulfedOBAsmodians = reward.getPlayersByRace(Race.ASMODIANS);
				for (EngulfedOphidianBridgePlayerReward asmoReward : engulfedOBAsmodians) {
					if (asmoReward != null) {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, asmoReward.getOwnerId());
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
