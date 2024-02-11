package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancescore.HarmonyArenaScore;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;

/**
 * @author xTz
 */
public class HarmonyScoreWriter extends InstanceScoreWriter<HarmonyArenaScore> {

	private final InstanceScoreType type;
	private Player owner;
	private int playerObjId;

	public HarmonyScoreWriter(HarmonyArenaScore reward, InstanceScoreType type, Player owner) {
		super(reward);
		this.type = type;
		this.owner = owner;
		this.playerObjId = owner.getObjectId();
	}

	public HarmonyScoreWriter(HarmonyArenaScore reward, InstanceScoreType type) {
		super(reward);
		this.type = type;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		HarmonyGroupReward harmonyGroupReward = instanceScore.getHarmonyGroupReward(playerObjId);

		writeC(buf, type.getId());
		switch (type) {
			case UPDATE_INSTANCE_PROGRESS:
				writeD(buf, 0);
				writeD(buf, instanceScore.getRound());
				break;
			case INIT_PLAYER:
				if (harmonyGroupReward == null)
					return;

				writeD(buf, harmonyGroupReward.getOwnerId());
				writeS(buf, harmonyGroupReward.getAGPlayer(playerObjId).getName(), 52); // playerName
				writeD(buf, harmonyGroupReward.getId()); // groupObj
				writeD(buf, owner.getObjectId()); // memberObj
				break;
			case UPDATE_PLAYER_BUFF_STATUS:
				writeD(buf, instanceScore.getPlayerReward(playerObjId).getRemaningTime()); // buffTime
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, playerObjId); // memberObj
				break;
			case SHOW_REWARD:
				if (harmonyGroupReward == null)
					return;

				writeD(buf, harmonyGroupReward.getBasicAP());
				writeD(buf, harmonyGroupReward.getScoreAP());
				writeD(buf, harmonyGroupReward.getRankingAP());
				writeD(buf, harmonyGroupReward.getBasicGP());
				writeD(buf, harmonyGroupReward.getScoreGP());
				writeD(buf, harmonyGroupReward.getRankingGP());
				writeD(buf, 186000137); // Courage Insignia
				writeD(buf, (int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(owner, harmonyGroupReward.getBasicCourage()));
				writeD(buf, (int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(owner, harmonyGroupReward.getScoreCourage()));
				writeD(buf, (int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(owner, harmonyGroupReward.getRankingCourage()));
				writeD(buf, 0); // Placeholder for Crucible Insignia
				writeD(buf, 0); // Basic Reward
				writeD(buf, 0); // Score Reward
				writeD(buf, 0); // Rank Reward
				RewardItem rewardItem1 = harmonyGroupReward.getRewardItem1();
				if (rewardItem1 != null) {
					writeD(buf, rewardItem1.getId());
					writeD(buf, (int) rewardItem1.getCount());
				} else {
					writeEmptyItem(buf);
				}
				RewardItem rewardItem2 = harmonyGroupReward.getRewardItem2();
				if (rewardItem2 != null) {
					writeD(buf, rewardItem2.getId());
					writeD(buf, (int) rewardItem2.getCount());
				} else {
					writeEmptyItem(buf);
				}
				break;
			case UPDATE_INSTANCE_BUFFS_AND_SCORE:
				writeD(buf, instanceScore.getLowerScoreCap());
				writeD(buf, instanceScore.getUpperScoreCap()); // capPoints
				writeD(buf, 3); // possible rounds
				writeD(buf, 1);
				writeD(buf, instanceScore.getBuffId());
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, instanceScore.getRound()); // round
				List<HarmonyGroupReward> groups = instanceScore.getHarmonyGroupInside();
				writeC(buf, groups.size()); // size
				for (HarmonyGroupReward group : groups) {
					writeC(buf, instanceScore.getRank(group.getPoints()));
					writeD(buf, group.getPvPKills());
					writeD(buf, group.getPoints()); // groupScore
					writeD(buf, group.getId()); // groupObj
					List<Player> members = instanceScore.getPlayersInside(group);
					writeC(buf, members.size());
					int i = 0;
					for (Player p : members) {
						PvPArenaPlayerReward rewardedPlayer = instanceScore.getPlayerReward(p.getObjectId());
						writeD(buf, 0);
						writeD(buf, rewardedPlayer.getRemaningTime()); // buffTime
						writeD(buf, 0);
						writeC(buf, group.getOwnerId()); // groupId
						writeC(buf, i); // memberNr
						writeH(buf, 0);
						writeS(buf, p.getName(), 52); // playerName
						writeD(buf, p.getObjectId()); // memberObj
						i++;
					}
				}
				break;
			case UPDATE_RANK:
				if (harmonyGroupReward == null)
					return;

				writeC(buf, instanceScore.getRank(harmonyGroupReward.getPoints()));
				writeD(buf, harmonyGroupReward.getPvPKills()); // kills
				writeD(buf, harmonyGroupReward.getPoints()); // groupScore
				writeD(buf, harmonyGroupReward.getId()); // groupObj
				break;
		}
	}

	private void writeEmptyItem(ByteBuffer buf) {
		writeD(buf, 0);
		writeD(buf, 0);
	}
}
