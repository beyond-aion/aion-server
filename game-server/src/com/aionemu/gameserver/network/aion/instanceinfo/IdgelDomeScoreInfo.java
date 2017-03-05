package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.IdgelDomeReward;
import com.aionemu.gameserver.model.instance.playerreward.IdgelDomePlayerReward;

/**
 * @author Ritsu
 */
public class IdgelDomeScoreInfo extends InstanceScoreInfo {

	private final IdgelDomeReward idgelDomeReward;
	private final int type;
	private final int objectId;
	private final InstanceScoreType instanceScoreType;

	public IdgelDomeScoreInfo(IdgelDomeReward idgelDomeReward, int type, int objectId) {
		this.idgelDomeReward = idgelDomeReward;
		this.type = type;
		this.objectId = objectId;
		this.instanceScoreType = idgelDomeReward.getInstanceScoreType();
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeC(buf, type);

		IdgelDomePlayerReward idgelDomePlayerReward = null;
		switch (type) {
			case 3:
				idgelDomePlayerReward = idgelDomeReward.getPlayerReward(objectId);
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, idgelDomePlayerReward.getOwnerId()); // objectId
				writeD(buf, idgelDomePlayerReward.getRace().equals(Race.ELYOS) ? 0 : 1); // elyos 0 asmodians 1
				break;
			case 5: // reward
				idgelDomePlayerReward = idgelDomeReward.getPlayerReward(objectId);
				writeD(buf, 100); // Participation
				writeD(buf, idgelDomePlayerReward.getBaseReward());
				writeD(buf, idgelDomePlayerReward.getBonusReward());
				writeD(buf, idgelDomePlayerReward.getGloryPoints());
				writeD(buf, 0);
				if (idgelDomePlayerReward.getFragmentedCeramium() > 0) { // item one
					writeD(buf, 186000243);
					writeD(buf, idgelDomePlayerReward.getFragmentedCeramium());
				} else {
					writeD(buf, 0);
					writeD(buf, 0);
				}
				writeD(buf, 0);

				if (idgelDomePlayerReward.getIdgelDomeBox() > 0) { // item two
					writeD(buf, 188053030);
					writeD(buf, idgelDomePlayerReward.getIdgelDomeBox());
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
				IdgelDomePlayerReward[] idgelDomeElyos = idgelDomeReward.getPlayersByRace(Race.ELYOS);
				for (IdgelDomePlayerReward reward : idgelDomeElyos) {
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
				IdgelDomePlayerReward[] idgelDomeAsmodians = idgelDomeReward.getPlayersByRace(Race.ASMODIANS);
				for (IdgelDomePlayerReward reward : idgelDomeAsmodians) {
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
				writeD(buf, idgelDomeReward.getElyosKills().intValue());
				writeD(buf, idgelDomeReward.getElyosPoints().intValue());
				writeD(buf, 0); // asmodians 0
				writeD(buf, instanceScoreType.isReinforsing() ? 1 : 0xFFFF);// 1 | 65535 - 0xFFFF | 0
				// asmodians reward
				writeC(buf, 0);
				writeD(buf, idgelDomeReward.getAsmodiansKills().intValue());
				writeD(buf, idgelDomeReward.getAsmodiansPoint().intValue());
				writeD(buf, 1); // elyos
				writeD(buf, instanceScoreType.isReinforsing() ? 1 : 0xFFFF); // 1 | 65535 - 0xFFFF | 0
				break;
			case 10:
				writeC(buf, 0);
				writeD(buf, idgelDomeReward.getKillsByRace(objectId == 0 ? Race.ELYOS : Race.ASMODIANS).intValue());
				writeD(buf, idgelDomeReward.getPointsByRace(objectId == 0 ? Race.ELYOS : Race.ASMODIANS).intValue());
				writeD(buf, objectId); // elyos 0 asmodians 1
				writeD(buf, 0);
				break;
		}
	}

}
