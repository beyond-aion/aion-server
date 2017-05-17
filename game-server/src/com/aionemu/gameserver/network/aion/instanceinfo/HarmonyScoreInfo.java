package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.instancereward.HarmonyArenaReward;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;

/**
 * @author xTz
 */
public class HarmonyScoreInfo extends InstanceScoreInfo {

	private final HarmonyArenaReward harmonyArena;
	private final int type;
	private final Player owner;

	public HarmonyScoreInfo(HarmonyArenaReward harmonyArena, int type, Player owner) {
		this.harmonyArena = harmonyArena;
		this.type = type;
		this.owner = owner;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		HarmonyGroupReward harmonyGroupReward = harmonyArena.getHarmonyGroupReward(owner.getObjectId());
		writeC(buf, type);
		switch (type) {
			case 2:
				writeD(buf, 0);
				writeD(buf, harmonyArena.getRound());
				break;
			case 3:
				writeD(buf, harmonyGroupReward.getOwnerId());
				writeS(buf, harmonyGroupReward.getAGPlayer(owner.getObjectId()).getName(), 52); // playerName
				writeD(buf, harmonyGroupReward.getId()); // groupObj
				writeD(buf, owner.getObjectId()); // memberObj
				break;
			case 4:
				writeD(buf, harmonyArena.getPlayerReward(owner.getObjectId()).getRemaningTime()); // buffTime
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, owner.getObjectId()); // memberObj
				break;
			case 5:
				writeD(buf, harmonyGroupReward.getBasicAP()); // basicRewardAp
				writeD(buf, harmonyGroupReward.getScoreAP()); // scoreRewardAp
				writeD(buf, harmonyGroupReward.getRankingAP()); // rankingRewardAp
				writeD(buf, harmonyGroupReward.getBasicGP()); // basicRewardGp
				writeD(buf, harmonyGroupReward.getScoreGP()); // scoreRewardGp
				writeD(buf, harmonyGroupReward.getRankingGP()); // rankingRewardGp
				writeD(buf, 186000137); // Courage Insignia
				writeD(buf, (int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(owner, harmonyGroupReward.getBasicCourage())); // basicRewardCourageIn
				writeD(buf, (int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(owner, harmonyGroupReward.getScoreCourage())); // scoreRewardCourageIn
				writeD(buf, (int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(owner, harmonyGroupReward.getRankingCourage())); // rankingRewardCourageIn
				if (harmonyGroupReward.getVictoryReward() != 0) {
					writeD(buf, 188052605); // 188052605
					writeD(buf, harmonyGroupReward.getVictoryReward());
				} else {
					writeD(buf, 0);
					writeD(buf, 0);
				}
				if (harmonyGroupReward.getArenaSupply() != 0) {
					writeD(buf, 188052181);
					writeD(buf, harmonyGroupReward.getArenaSupply());
				} else {
					writeD(buf, 0);
					writeD(buf, 0);
				}
				if (harmonyGroupReward.getConsolationReward() != 0) {
					writeD(buf, 188052606); // 188052606
					writeD(buf, harmonyGroupReward.getConsolationReward());
				} else {
					writeD(buf, 0);
					writeD(buf, 0);
				}
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, (int) harmonyGroupReward.getParticipation() * 100); // progressType
				writeD(buf, harmonyGroupReward.getPoints()); // score
				break;
			case 6:
				writeD(buf, 0);
				writeD(buf, harmonyArena.getCapPoints()); // capPoints
				writeD(buf, 3); // possible rounds
				writeD(buf, 1);
				writeD(buf, harmonyArena.getBuffId());
				writeD(buf, 0);
				writeD(buf, 0);
				writeD(buf, harmonyArena.getRound()); // round
				List<HarmonyGroupReward> groups = harmonyArena.getHarmonyGroupInside();
				writeC(buf, groups.size()); // size
				for (HarmonyGroupReward group : groups) {
					writeC(buf, harmonyArena.getRank(group.getPoints()));
					writeD(buf, group.getPvPKills());
					writeD(buf, group.getPoints()); // groupScore
					writeD(buf, group.getId()); // groupObj
					List<Player> members = harmonyArena.getPlayersInside(group);
					writeC(buf, members.size());
					int i = 0;
					for (Player p : members) {
						PvPArenaPlayerReward rewardedPlayer = harmonyArena.getPlayerReward(p.getObjectId());
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
			case 10:
				writeC(buf, harmonyArena.getRank(harmonyGroupReward.getPoints()));
				writeD(buf, harmonyGroupReward.getPvPKills()); // kills
				writeD(buf, harmonyGroupReward.getPoints()); // groupScore
				writeD(buf, harmonyGroupReward.getId()); // groupObj
				break;
		}
	}

}
