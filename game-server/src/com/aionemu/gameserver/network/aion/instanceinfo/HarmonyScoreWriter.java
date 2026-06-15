package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancescore.HarmonyArenaScore;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.templates.rewards.ArenaRewardItem;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;

/**
 * @author xTz
 */
public class HarmonyScoreWriter extends InstanceScoreWriter<HarmonyArenaScore> {

	private final InstanceScoreType type;
	private int playerObjId;

	public HarmonyScoreWriter(HarmonyArenaScore reward, InstanceScoreType type, Player owner) {
		super(reward);
		this.type = type;
		this.playerObjId = owner.getObjectId();
	}

	public HarmonyScoreWriter(HarmonyArenaScore reward, InstanceScoreType type) {
		super(reward);
		this.type = type;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		HarmonyGroupReward harmonyGroupReward = instanceScore.getGroupReward(playerObjId);
		writeC(buf, type.getId());
		switch (type) {
			case UPDATE_INSTANCE_PROGRESS:
				writeD(buf, 0);
				writeD(buf, instanceScore.getRound());
				break;
			case INIT_PLAYER:
				if (harmonyGroupReward == null) {
					writeB(buf, new byte[64]);
					return;
				}

				writeD(buf, harmonyGroupReward.getOwnerId());
				writeS(buf, harmonyGroupReward.getAGPlayer(playerObjId).name(), 52); // playerName
				writeD(buf, harmonyGroupReward.getGrpObjectId()); // groupObj
				writeD(buf, playerObjId); // memberObj
				break;
			case UPDATE_PLAYER_BUFF_STATUS:
				PvPArenaPlayerReward reward = instanceScore.getPlayerReward(playerObjId);
				if (reward == null) {
					writeB(buf, new byte[16]);
					return;
				}

				writeD(buf, reward.getRemainingTime()); // buffTime
				writeD(buf, reward.hasBoostMorale() ? instanceScore.getBuffId() : 0); // Buff ID
				writeD(buf, 0);
				writeD(buf, playerObjId); // memberObj
				break;
			case SHOW_REWARD:
				PvPArenaPlayerReward playerReward = instanceScore.getPlayerReward(playerObjId);
				if (playerReward == null) {
					writeB(buf, new byte[72]);
					return;
				}

				writePoints(buf, playerReward.getAp());
				writePoints(buf, playerReward.getGp());
				writeReward(buf, playerReward.getCourageInsignia());
				writeReward(buf, playerReward.getCrucibleInsignia());
				writeSimpleReward(buf, playerReward.getRewardItem1());
				writeSimpleReward(buf, playerReward.getRewardItem2());
				break;
			case UPDATE_INSTANCE_BUFFS_AND_SCORE:
				writeD(buf, instanceScore.getLowerScoreCap());
				writeD(buf, instanceScore.getUpperScoreCap()); // capPoints
				writeD(buf, 3); // possible rounds
				writeD(buf, 1);
				writeD(buf, 0); // Global Buff Id
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, instanceScore.getRound()); // round
				List<HarmonyGroupReward> groups = instanceScore.getHarmonyGroupInside();
				writeC(buf, groups.size()); // size
				int groupIndex = 0;
				for (HarmonyGroupReward group : groups) {
					writeC(buf, instanceScore.getRank(group));
					writeD(buf, group.getPvPKills());
					writeD(buf, group.getPoints()); // groupScore
					writeD(buf, group.getGrpObjectId()); // groupObj
					List<Player> members = instanceScore.getPlayersInside(group);
					writeC(buf, members.size());
					int i = 0;
					for (Player p : members) {
						PvPArenaPlayerReward apr = instanceScore.getPlayerReward(p.getObjectId());
						writeD(buf, 0);
						writeD(buf, apr.getRemainingTime());
						writeD(buf, 0); // Probably Buff id?
						writeC(buf, groupIndex); // groupIndex, necessary for group names
						writeC(buf, i); // memberNr
						writeH(buf, 0);
						writeS(buf, p.getName(), 52); // playerName
						writeD(buf, p.getObjectId()); // memberObj
						i++;
					}
					groupIndex++;
				}
				break;
			case UPDATE_RANK:
				if (harmonyGroupReward == null) {
					writeB(buf, new byte[13]);
					return;
				}

				writeC(buf, instanceScore.getRank(harmonyGroupReward));
				writeD(buf, harmonyGroupReward.getPvPKills()); // kills
				writeD(buf, harmonyGroupReward.getPoints()); // groupScore
				writeD(buf, harmonyGroupReward.getGrpObjectId()); // groupObj
				break;
		}
	}

	private void writePoints(ByteBuffer buf, ArenaRewardItem rewardItem) {
		writeD(buf, rewardItem.baseCount());
		writeD(buf, rewardItem.scoreCount());
		writeD(buf, rewardItem.rankingCount());
	}

	private void writeReward(ByteBuffer buf, ArenaRewardItem rewardItem) {
		writeD(buf, rewardItem.itemId());
		writeD(buf, rewardItem.baseCount());
		writeD(buf, rewardItem.scoreCount());
		writeD(buf, rewardItem.rankingCount());
	}

	private void writeSimpleReward(ByteBuffer buf, RewardItem rewardItem) {
		if (rewardItem != null) {
			writeD(buf, rewardItem.getId());
			writeD(buf, (int) rewardItem.getCount());
		} else {
			writeD(buf, 0);
			writeD(buf, 0);
		}
	}
}
