package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.PvpInstanceReward;
import com.aionemu.gameserver.model.instance.playerreward.PvpInstancePlayerReward;

/**
 * @author Estrayl
 */
public class PvpInstanceScoreInfo extends InstanceScoreInfo<PvpInstanceReward<PvpInstancePlayerReward>> {

	private static final int MAXIMUM_PLAYER_COUNT_PER_FACTION = 24;
	private final InstanceScoreType instanceScoreType;
	private List<Player> participants;
	private Race race;
	private int objectId;
	private int status;

	public PvpInstanceScoreInfo(PvpInstanceReward<PvpInstancePlayerReward> reward, InstanceScoreType instanceScoreType) {
		super(reward);
		this.instanceScoreType = instanceScoreType;
	}

	public PvpInstanceScoreInfo(PvpInstanceReward<PvpInstancePlayerReward> reward, InstanceScoreType instanceScoreType, Race race) {
		super(reward);
		this.instanceScoreType = instanceScoreType;
		this.race = race;
	}

	public PvpInstanceScoreInfo(PvpInstanceReward<PvpInstancePlayerReward> reward, InstanceScoreType instanceScoreType, int objectId, int status) {
		super(reward);
		this.instanceScoreType = instanceScoreType;
		this.objectId = objectId;
		this.status = status;
	}

	public PvpInstanceScoreInfo(PvpInstanceReward<PvpInstancePlayerReward> reward, InstanceScoreType instanceScoreType, List<Player> participants) {
		super(reward);
		this.instanceScoreType = instanceScoreType;
		this.participants = participants;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeC(buf, instanceScoreType.getId());
		switch (instanceScoreType) {
			case UPDATE_INSTANCE_PROGRESS -> writeD(buf, reward.getInstanceProgressionType() == InstanceProgressionType.START_PROGRESS ? 0 : 2);
			case INIT_PLAYER -> {
				writeD(buf, 0); // unk
				writeD(buf, status); // player is dead: 60 else 0
				writeD(buf, objectId);
				writeD(buf, reward.getPlayerReward(objectId).getRace().getRaceId());
			}
			case UPDATE_PLAYER_BUFF_STATUS -> {
				writeD(buf, 0);
				writeD(buf, status);// Spawn(0) - Dead(60)
				writeD(buf, objectId); // PlayerObjectId
			}
			case SHOW_REWARD -> writeShowReward(buf);
			case UPDATE_INSTANCE_BUFFS_AND_SCORE -> writeUpdateScoreToBuffer(buf);
			case UPDATE_ALL_PLAYER_INFO -> writeUpdatePlayerInfoToBuffer(buf);
			case PLAYER_QUIT -> writeD(buf, objectId);
			case UPDATE_FACTION_SCORE -> {
				writeC(buf, 0);
				writeD(buf, race == Race.ELYOS ? reward.getElyosKills() : reward.getAsmodiansKills());
				writeD(buf, race == Race.ELYOS ? reward.getElyosPoints() : reward.getAsmodiansPoints());
				writeD(buf, race.getRaceId());
				writeD(buf, reward.getRaceWithHighestPoints().getRaceId());
			}
		}
	}

	private void writeShowReward(ByteBuffer buf) {
		PvpInstancePlayerReward info = reward.getPlayerReward(objectId);
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
	}

	private void writeUpdateScoreToBuffer(ByteBuffer buf) {
		writeD(buf, 100); // Should be differentiated between asmos and elyos
		List<Player> elyos = participants.stream().filter(p -> p.getRace() == Race.ELYOS).toList();
		for (Player p : elyos) {
			writeD(buf, 0); // should be instance buff ID
			writeD(buf, p.isDead() ? 60 : 0); // was previously used for remaining instance buff time
			writeD(buf, p.getObjectId());
		}
		writeEmptyDataToBuffer(buf, 12, MAXIMUM_PLAYER_COUNT_PER_FACTION - elyos.size());

		List<Player> asmodians = participants.stream().filter(p -> p.getRace() == Race.ASMODIANS).toList();
		for (Player p : asmodians) {
			writeD(buf, 0); // should be instance buff ID
			writeD(buf, p.isDead() ? 60 : 0); // was previously used for remaining instance buff time
			writeD(buf, p.getObjectId());
		}
		writeEmptyDataToBuffer(buf, 12, MAXIMUM_PLAYER_COUNT_PER_FACTION - asmodians.size());
		writeC(buf, 0);
		writeD(buf, reward.getElyosKills());
		writeD(buf, reward.getElyosPoints());
		writeD(buf, Race.ELYOS.getRaceId());
		writeD(buf, 0);
		writeC(buf, 0);
		writeD(buf, reward.getAsmodiansKills());
		writeD(buf, reward.getAsmodiansPoints());
		writeD(buf, Race.ASMODIANS.getRaceId());
		writeD(buf, reward.getRaceWithHighestPoints().getRaceId());
	}

	private void writeUpdatePlayerInfoToBuffer(ByteBuffer buf) {
		List<Player> elyos = participants.stream().filter(p -> p.getRace() == Race.ELYOS).toList();
		for (Player p : elyos) { // 69 bytes per player
			PvpInstancePlayerReward pi = reward.getPlayerReward(p.getObjectId());
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
		writeEmptyDataToBuffer(buf, 69, MAXIMUM_PLAYER_COUNT_PER_FACTION - elyos.size());
		List<Player> asmodians = participants.stream().filter(p -> p.getRace() == Race.ASMODIANS).toList();
		for (Player p : asmodians) { // 69 bytes per player
			PvpInstancePlayerReward pi = reward.getPlayerReward(p.getObjectId());
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
		writeEmptyDataToBuffer(buf, 69, MAXIMUM_PLAYER_COUNT_PER_FACTION - asmodians.size());
	}

	private void writeEmptyDataToBuffer(ByteBuffer buf, int dataSize, int missingPlayerCount) {
		writeB(buf, new byte[dataSize * missingPlayerCount]);
	}
}
