package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.EngulfedOphidianBridgeReward;
import com.aionemu.gameserver.model.instance.playerreward.EngulfedOphidianBridgePlayerReward;

/**
 * @author xTz
 */
public class EngulfedOphidianBridgeScoreInfo extends InstanceScoreInfo {

	private final EngulfedOphidianBridgeReward engulfedOBReward;
	private final int type;
	private final Integer object;
	private final InstanceScoreType instanceScoreType;

	public EngulfedOphidianBridgeScoreInfo(EngulfedOphidianBridgeReward engulfedOBReward, int type, Integer object) {
		this.engulfedOBReward = engulfedOBReward;
		this.type = type;
		this.object = object;
		this.instanceScoreType = engulfedOBReward.getInstanceScoreType();
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeC(buf, type);
		EngulfedOphidianBridgePlayerReward engulfedOBPlayerReward = null;
		switch (type) {
			case 3:
				engulfedOBPlayerReward = engulfedOBReward.getPlayerReward(object);
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, engulfedOBPlayerReward.getOwner()); // objectId
				writeD(buf, engulfedOBPlayerReward.getRace().equals(Race.ELYOS) ? 0 : 1); // elyos 0 asmodians 1
				break;
			case 5: // reward
				engulfedOBPlayerReward = engulfedOBReward.getPlayerReward(object);
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
				EngulfedOphidianBridgePlayerReward[] engulfedOBElyos = engulfedOBReward.getPlayersByRace(Race.ELYOS);
				for (int i = 0; i < engulfedOBElyos.length; i++) {
					EngulfedOphidianBridgePlayerReward reward = engulfedOBElyos[i];
					if (reward != null) {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, reward.getOwner());
					} else {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, 0);
					}
				}
				EngulfedOphidianBridgePlayerReward[] engulfedOBAsmodians = engulfedOBReward.getPlayersByRace(Race.ASMODIANS);
				for (int i = 0; i < engulfedOBAsmodians.length; i++) {
					EngulfedOphidianBridgePlayerReward reward = engulfedOBAsmodians[i];
					if (reward != null) {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, reward.getOwner());
					} else {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, 0);
					}
				}
				// elyos reward
				writeC(buf, 0);
				writeD(buf, engulfedOBReward.getElyosKills().intValue());
				writeD(buf, engulfedOBReward.getElyosPoints().intValue());
				writeD(buf, 0); // asmodians 0
				writeD(buf, instanceScoreType.isReinforsing() ? 1 : 0xFFFF);// 1 | 65535 - 0xFFFF | 0
				// asmodians reward
				writeC(buf, 0);
				writeD(buf, engulfedOBReward.getAsmodiansKills().intValue());
				writeD(buf, engulfedOBReward.getAsmodiansPoint().intValue());
				writeD(buf, 1); // elyos
				writeD(buf, instanceScoreType.isReinforsing() ? 1 : 0xFFFF); // 1 | 65535 - 0xFFFF | 0
				break;
			case 10:
				writeC(buf, 0);
				writeD(buf, engulfedOBReward.getKillsByRace(object == 0 ? Race.ELYOS : Race.ASMODIANS).intValue());
				writeD(buf, engulfedOBReward.getPointsByRace(object == 0 ? Race.ELYOS : Race.ASMODIANS).intValue());
				writeD(buf, object); // elyos 0 asmodians 1
				writeD(buf, 0);
				break;
		}
	}

}
