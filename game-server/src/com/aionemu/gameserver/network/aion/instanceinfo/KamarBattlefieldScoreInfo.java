package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.instancereward.KamarReward;
import com.aionemu.gameserver.model.instance.playerreward.KamarPlayerReward;

/**
 * @author xTz
 */
public class KamarBattlefieldScoreInfo extends InstanceScoreInfo<KamarReward> {

	private final int type;
	private final int objectId;

	public KamarBattlefieldScoreInfo(KamarReward reward, int type, int objectId) {
		super(reward);
		this.type = type;
		this.objectId = objectId;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeC(buf, type);

		KamarPlayerReward kamarPlayerReward;
		switch (type) {
			case 3:
				kamarPlayerReward = reward.getPlayerReward(objectId);
				writeD(buf, 10); // buff id
				writeD(buf, kamarPlayerReward.getRemaningTime()); // bufftime
				writeD(buf, kamarPlayerReward.getOwnerId()); // objectId
				writeD(buf, kamarPlayerReward.getRace() == Race.ELYOS ? 0 : 1);
				break;
			case 4:
				kamarPlayerReward = reward.getPlayerReward(objectId);
				writeD(buf, 10); // buff id
				writeD(buf, kamarPlayerReward.getRemaningTime()); // bufftime
				writeD(buf, kamarPlayerReward.getOwnerId()); // objectId
				break;
			case 5: // reward
				kamarPlayerReward = reward.getPlayerReward(objectId);
				writeD(buf, 100); // partitipation
				writeD(buf, kamarPlayerReward.getBaseReward());
				writeD(buf, kamarPlayerReward.getBonusReward());
				writeD(buf, kamarPlayerReward.getGloryPoints());
				writeD(buf, 0);
				if (kamarPlayerReward.getKamarBox() > 0) {
					writeD(buf, 188052670);
					writeD(buf, kamarPlayerReward.getKamarBox());
				} else {
					writeD(buf, 0);
					writeD(buf, 0);
				}
				writeD(buf, 0);
				if (kamarPlayerReward.getBloodMarks() > 0) {
					writeD(buf, 1860000236);
					writeD(buf, kamarPlayerReward.getBloodMarks());
				} else {
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
				KamarPlayerReward[] kamarElyos = reward.getPlayersByRace(Race.ELYOS);
				for (KamarPlayerReward reward : kamarElyos) {
					if (reward != null) {
						writeD(buf, 10); // buff id
						writeD(buf, reward.getRemaningTime()); // bufftime
						writeD(buf, reward.getOwnerId());
					} else {
						writeD(buf, 0);
						writeD(buf, 0);
						writeD(buf, 0);
					}
				}
				KamarPlayerReward[] kamarAsmodians = reward.getPlayersByRace(Race.ASMODIANS);
				for (KamarPlayerReward reward : kamarAsmodians) {
					if (reward != null) {
						writeD(buf, 10); // buff id
						writeD(buf, reward.getRemaningTime()); // bufftime
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
