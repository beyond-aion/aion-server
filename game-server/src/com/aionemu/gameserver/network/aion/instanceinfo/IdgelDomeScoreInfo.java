package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.IdgelDomeInfo;
import com.aionemu.gameserver.model.instance.playerreward.IdgelDomePlayerInfo;

/**
 * @author Ritsu, Estrayl
 */
public class IdgelDomeScoreInfo extends InstanceScoreInfo<IdgelDomeInfo> {

	private final InstanceScoreType ist;
	private List<Player> participants;
	private Race race;
	private int objectId;
	private int status;

	public IdgelDomeScoreInfo(IdgelDomeInfo idi, InstanceScoreType ist) {
		super(idi);
		this.ist = ist;
	}

	public IdgelDomeScoreInfo(IdgelDomeInfo idi, InstanceScoreType ist, Race race) {
		super(idi);
		this.ist = ist;
		this.race = race;
	}

	public IdgelDomeScoreInfo(IdgelDomeInfo idi, InstanceScoreType ist, int objectId, int status) {
		super(idi);
		this.ist = ist;
		this.objectId = objectId;
		this.status = status;
	}

	public IdgelDomeScoreInfo(IdgelDomeInfo idi, InstanceScoreType ist, List<Player> participants) {
		super(idi);
		this.ist = ist;
		this.participants = participants;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeC(buf, ist.getId());
		switch (ist) {
			case UPDATE_PROGRESS:
				writeD(buf, reward.getInstanceProgressionType() == InstanceProgressionType.START_PROGRESS ? 0 : 2);
				break;
			case INIT_PLAYER:
				writeD(buf, 0); // unk
				writeD(buf, status); // player is dead: 60 else 0
				writeD(buf, objectId);
				writeD(buf, reward.getPlayerReward(objectId).getRace().getRaceId());
				break;
			case UPDATE_PLAYER_STATUS:
				writeD(buf, 0);
				writeD(buf, status);// Spawn(0) - Dead(60)
				writeD(buf, objectId); // PlayerObjectId
				break;
			case SHOW_REWARD: // reward
				IdgelDomePlayerInfo info = reward.getPlayerReward(objectId);
				writeD(buf, 100); // Participation
				writeD(buf, info.getBaseAp());
				writeD(buf, info.getBonusAp());
				writeD(buf, info.getBaseGp());
				writeD(buf, info.getBonusGp());
				writeD(buf, info.getReward1ItemId());
				writeD(buf, info.getReward1Count());
				writeD(buf, info.getReward1BonusCount());
				writeD(buf, info.getReward2ItemId());
				writeD(buf, info.getReward2Count());
				writeD(buf, info.getReward2BonusCount());
				writeD(buf, info.getReward3ItemId());
				writeD(buf, info.getReward3Count());
				writeD(buf, info.getReward4ItemId());
				writeD(buf, info.getReward4Count());
				writeD(buf, info.getBonusRewardItemId());
				writeD(buf, info.getBonusRewardCount());
				writeC(buf, info.getBonusRewardItemId() > 0 ? 1 : 0); // showBonusReward flag
				break;
			case UPDATE_SCORE:
				writeD(buf, 100);
				List<Player> elyos = participants.stream().filter(p -> p.getRace() == Race.ELYOS).toList();
				for (Player p : elyos) {
					writeD(buf, 0); // unk
					writeD(buf, p.isDead() ? 60 : 0);
					writeD(buf, p.getObjectId());
				}
				writeB(buf, new byte[12 * (24 - elyos.size())]);
				List<Player> asmodians = participants.stream().filter(p -> p.getRace() == Race.ASMODIANS).toList();
				for (Player p : asmodians) {
					writeD(buf, 0); // unk
					writeD(buf, p.isDead() ? 60 : 0);
					writeD(buf, p.getObjectId());
				}
				writeB(buf, new byte[12 * (24 - asmodians.size())]);
				// Elyos status
				writeC(buf, 0);
				writeD(buf, reward.getElyosKills());
				writeD(buf, reward.getElyosPoints());
				writeD(buf, 0); // asmodians
				writeD(buf, reward.getInstanceProgressionType().isPreparing() ? 0xFFFF : 1);
				// Asmodian status
				writeC(buf, 0);
				writeD(buf, reward.getAsmodiansKills());
				writeD(buf, reward.getAsmodiansPoints());
				writeD(buf, 1); // elyos
				writeD(buf, reward.getInstanceProgressionType().isPreparing() ? 0xFFFF : 1);
				break;
			case UPDATE_PLAYER_INFO:
				List<Player> elys = participants.stream().filter(p -> p.getRace() == Race.ELYOS).toList();
				for (Player p : elys) { // 69 bytes per player
					IdgelDomePlayerInfo pi = reward.getPlayerReward(p.getObjectId());
					if (pi == null)
						continue;
					writeD(buf, p.getObjectId());
					writeC(buf, p.getPlayerClass().getClassId());
					writeC(buf, p.getAbyssRank().getRank().getId());
					writeC(buf, 0);
					writeH(buf, 0);
					writeD(buf, pi.getPvPKills());
					writeD(buf, pi.getPoints());
					writeS(buf, p.getName(), 52);
				}
				writeB(buf, new byte[69 * (24 - elys.size())]); // places
				List<Player> asmos = participants.stream().filter(p -> p.getRace() == Race.ASMODIANS).toList();
				for (Player p : asmos) { // 69 bytes per player
					IdgelDomePlayerInfo pi = reward.getPlayerReward(p.getObjectId());
					if (pi == null)
						continue;
					writeD(buf, p.getObjectId());
					writeC(buf, p.getPlayerClass().getClassId());
					writeC(buf, p.getAbyssRank().getRank().getId());
					writeC(buf, 0);
					writeH(buf, 0);
					writeD(buf, pi.getPvPKills());
					writeD(buf, pi.getPoints());
					writeS(buf, p.getName(), 52);
				}
				writeB(buf, new byte[69 * (24 - asmos.size())]);
				break;
			case PLAYER_QUIT:
				writeD(buf, objectId);
				break;
			case NPC_DIED:
				writeC(buf, 0);
				writeD(buf, race == Race.ELYOS ? reward.getElyosKills() : reward.getAsmodiansKills());
				writeD(buf, race == Race.ELYOS ? reward.getElyosPoints() : reward.getAsmodiansPoints());
				writeD(buf, race.getRaceId());
				writeD(buf, reward.getAsmodiansPoints() == reward.getElyosPoints() ? 0xFFFF : 0);
				break;
		}
	}

}
