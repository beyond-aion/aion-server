package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancescore.PvpInstanceScore;
import com.aionemu.gameserver.model.instance.playerreward.PvpInstancePlayerReward;

/**
 * @author Estrayl
 */
public class PvpInstanceScoreWriter extends InstanceScoreWriter<PvpInstanceScore<PvpInstancePlayerReward>> {

	private static final int MAXIMUM_PLAYER_COUNT_PER_FACTION = 24;
	private final InstanceScoreType instanceScoreType;
	private List<Player> participants;
	private Race race;
	private int objectId;
	private int status;

	public PvpInstanceScoreWriter(PvpInstanceScore<PvpInstancePlayerReward> reward, InstanceScoreType instanceScoreType) {
		super(reward);
		this.instanceScoreType = instanceScoreType;
	}

	public PvpInstanceScoreWriter(PvpInstanceScore<PvpInstancePlayerReward> reward, InstanceScoreType instanceScoreType, Race race) {
		super(reward);
		this.instanceScoreType = instanceScoreType;
		this.race = race;
	}

	public PvpInstanceScoreWriter(PvpInstanceScore<PvpInstancePlayerReward> reward, InstanceScoreType instanceScoreType, int objectId, int status) {
		super(reward);
		this.instanceScoreType = instanceScoreType;
		this.objectId = objectId;
		this.status = status;
	}

	public PvpInstanceScoreWriter(PvpInstanceScore<PvpInstancePlayerReward> reward, InstanceScoreType instanceScoreType, List<Player> participants) {
		super(reward);
		this.instanceScoreType = instanceScoreType;
		this.participants = participants;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeC(buf, instanceScoreType.getId());
		switch (instanceScoreType) {
			case UPDATE_INSTANCE_PROGRESS -> writeD(buf, instanceScore.getInstanceProgressionType() == InstanceProgressionType.START_PROGRESS ? 0 : 2);
			case INIT_PLAYER -> {
				writeD(buf, 0); // unk
				writeD(buf, status); // player is dead: 60 else 0
				writeD(buf, objectId);
				writeD(buf, instanceScore.getPlayerReward(objectId).getRace().getRaceId());
			}
			case UPDATE_PLAYER_BUFF_STATUS -> {
				writeD(buf, 0);
				writeD(buf, status);// Spawn(0) - Dead(60)
				writeD(buf, objectId); // PlayerObjectId
			}
			case SHOW_REWARD -> writeShowReward(buf);
			case UPDATE_INSTANCE_BUFFS_AND_SCORE -> writeUpdateScoreToBuffer(buf);
			case UPDATE_ALL_PLAYER_INFO -> writePlayerFullData(buf);
			case PLAYER_QUIT -> writeD(buf, objectId);
			case UPDATE_FACTION_SCORE -> {
				writeC(buf, 0);
				writeD(buf, race == Race.ELYOS ? instanceScore.getElyosKills() : instanceScore.getAsmodiansKills());
				writeD(buf, race == Race.ELYOS ? instanceScore.getElyosPoints() : instanceScore.getAsmodiansPoints());
				writeD(buf, race.getRaceId());
				writeD(buf, instanceScore.getRaceWithHighestPoints().getRaceId());
			}
		}
	}

	private void writeShowReward(ByteBuffer buf) {
		PvpInstancePlayerReward reward = instanceScore.getPlayerReward(objectId);
		writeD(buf, 100); // Participation
		writeD(buf, reward.getBaseAp());
		writeD(buf, reward.getBonusAp());
		writeD(buf, reward.getBaseGp());
		writeD(buf, reward.getBonusGp());
		writeD(buf, reward.getReward1ItemId());
		writeD(buf, reward.getReward1Count());
		writeD(buf, reward.getReward1BonusCount());
		writeD(buf, reward.getReward2ItemId());
		writeD(buf, reward.getReward2Count());
		writeD(buf, reward.getReward2BonusCount());
		writeD(buf, reward.getReward3ItemId());
		writeD(buf, reward.getReward3Count());
		writeD(buf, reward.getReward4ItemId());
		writeD(buf, reward.getReward4Count());
		writeD(buf, reward.getBonusRewardItemId());
		writeD(buf, reward.getBonusRewardCount());
		writeC(buf, reward.getBonusRewardItemId() > 0 ? 1 : 0); // showBonusReward flag
	}

	private void writeUpdateScoreToBuffer(ByteBuffer buf) {
		writeD(buf, 100); // Should be differentiated between asmos and elyos
		writePlayerBuffInfo(buf, participants.stream().filter(p -> p.getRace() == Race.ELYOS).toList());
		writePlayerBuffInfo(buf, participants.stream().filter(p -> p.getRace() == Race.ASMODIANS).toList());
		writeC(buf, 0);
		writeD(buf, instanceScore.getElyosKills());
		writeD(buf, instanceScore.getElyosPoints());
		writeD(buf, Race.ELYOS.getRaceId());
		writeD(buf, 0);
		writeC(buf, 0);
		writeD(buf, instanceScore.getAsmodiansKills());
		writeD(buf, instanceScore.getAsmodiansPoints());
		writeD(buf, Race.ASMODIANS.getRaceId());
		writeD(buf, instanceScore.getRaceWithHighestPoints().getRaceId());
	}

	private void writePlayerFullData(ByteBuffer buf) {
		writePlayerFullData(buf, participants.stream().filter(p -> p.getRace() == Race.ELYOS).toList());
		writePlayerFullData(buf, participants.stream().filter(p -> p.getRace() == Race.ASMODIANS).toList());
	}

	private void writePlayerFullData(ByteBuffer buf, List<Player> players) {
		for (Player player : players) { // 69 bytes per player
			PvpInstancePlayerReward reward = instanceScore.getPlayerReward(player.getObjectId());
			if (reward == null)
				continue;
			writeD(buf, player.getObjectId());
			writeC(buf, player.getPlayerClass().getClassId());
			writeC(buf, player.getAbyssRank().getRank().getId());
			writeC(buf, 0);
			writeH(buf, 0);
			writeD(buf, reward.getPvPKills());
			writeD(buf, reward.getPoints());
			writeS(buf, player.getName(), 52);
		}
		writeEmptyDataToBuffer(buf, 69, MAXIMUM_PLAYER_COUNT_PER_FACTION - players.size());
	}

	private void writePlayerBuffInfo(ByteBuffer buf, List<Player> players) {
		for (Player player : players) {
			writeD(buf, 0); // should be instance buff ID
			writeD(buf, player.isDead() ? 60 : 0); // was previously used for remaining instance buff time
			writeD(buf, player.getObjectId());
		}
		writeEmptyDataToBuffer(buf, 12, MAXIMUM_PLAYER_COUNT_PER_FACTION - players.size());
	}

	private void writeEmptyDataToBuffer(ByteBuffer buf, int dataSize, int missingPlayerCount) {
		writeB(buf, new byte[dataSize * missingPlayerCount]);
	}
}
