package com.aionemu.gameserver.model.instance.instancescore;

import java.util.*;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instanceposition.*;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class PvPArenaScore extends InstanceScore<PvPArenaPlayerReward> {

	private Map<Integer, Boolean> positions = new HashMap<>();
	private List<Integer> zones = new ArrayList<>();
	private int round = 1;
	private Integer zone;
	private int bonusTime;
	private int capPoints;
	private long instanceTime;
	private final byte buffId;
	protected WorldMapInstance instance;
	private GeneralInstancePosition instancePosition;

	public PvPArenaScore(WorldMapInstance instance) {
		this.instance = instance;
		boolean isSolo = isSoloArena();
		capPoints = isSolo ? 14400 : 50000;
		bonusTime = isSolo ? 8100 : 12000;
		Collections.addAll(zones, isSolo ? new Integer[] { 1, 2, 3, 4 } : new Integer[] { 1, 2, 3, 4, 5, 6 });
		int positionSize;
		if (isSolo) {
			positionSize = 4;
			buffId = 8;
			instancePosition = new DisciplineInstancePosition();
		} else if (isGlory()) {
			buffId = 7;
			positionSize = 8;
			instancePosition = new GloryInstancePosition();
		} else if (instance.getMapId() == 300450000 || instance.getMapId() == 300570000) {
			buffId = 7;
			positionSize = 12;
			instancePosition = new HarmonyInstancePosition();
		} else {
			buffId = 7;
			positionSize = 12;
			instancePosition = new ChaosInstancePosition();
		}
		instancePosition.initialize(instance.getMapId(), instance.getInstanceId());
		for (int i = 1; i <= positionSize; i++) {
			positions.put(i, Boolean.FALSE);
		}
		setRndZone();
	}

	public final boolean isSoloArena() {
		return instance.getMapId() == 300430000 || instance.getMapId() == 300360000;
	}

	public final boolean isGlory() {
		return instance.getMapId() == 300550000;
	}

	public int getCapPoints() {
		return capPoints;
	}

	public final void setRndZone() {
		int index = Rnd.get(zones.size());
		zone = zones.remove(index);
	}

	private List<Integer> getFreePositions() {
		List<Integer> p = new ArrayList<>();
		for (Integer key : positions.keySet()) {
			if (!positions.get(key)) {
				p.add(key);
			}
		}
		return p;
	}

	public synchronized void setRndPosition(int objectId) {
		PvPArenaPlayerReward reward = getPlayerReward(objectId);
		int position = reward.getPosition();
		if (position != 0) {
			clearPosition(position, Boolean.FALSE);
		}
		List<Integer> freePositions = getFreePositions();
		if (!freePositions.isEmpty()) {
			int key = Rnd.get(freePositions);
			clearPosition(key, Boolean.TRUE);
			reward.setPosition(key);
		}
	}

	public synchronized void clearPosition(int position, Boolean result) {
		positions.put(position, result);
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public boolean regPlayerReward(Player player) {
		if (!containsPlayer(player.getObjectId())) {
			addPlayerReward(new PvPArenaPlayerReward(player, bonusTime, buffId));
			return true;
		}
		return false;
	}

	@Override
	public void addPlayerReward(PvPArenaPlayerReward reward) {
		super.addPlayerReward(reward);
	}

	public void portToPosition(Player player) {
		int objectId = player.getObjectId();
		regPlayerReward(player);
		setRndPosition(objectId);
		PvPArenaPlayerReward playerReward = getPlayerReward(objectId);
		playerReward.applyBoostMoraleEffect(player);
		instancePosition.port(player, zone, playerReward.getPosition());
	}

	public boolean canRewardOpportunityToken(PvPArenaPlayerReward rewardedPlayer) {
		if (rewardedPlayer != null) {
			int rank = getRank(rewardedPlayer.getScorePoints());
			return isSoloArena() && rank == 1 || rank > 2;
		}
		return false;
	}

	public int getRank(int points) {
		List<PvPArenaPlayerReward> rewardsSortedByScorePoints = getPlayerRewards().stream().sorted((r1, r2) -> Integer.compare(r2.getScorePoints(), r1.getScorePoints()))
			.toList();
		int rank = -1;
		for (PvPArenaPlayerReward reward : rewardsSortedByScorePoints) {
			if (reward.getScorePoints() >= points) {
				rank++;
			}
		}
		return rank;
	}

	public boolean hasCapPoints() {
		IntSummaryStatistics points = getPlayerRewards().stream().mapToInt(InstancePlayerReward::getPoints).summaryStatistics();
		int maxPoints = points.getMax();
		if (isSoloArena() && maxPoints - points.getMin() >= 1500)
			return true;
		return maxPoints >= capPoints;
	}

	public int getTotalPoints() {
		return getPlayerRewards().stream().mapToInt(PvPArenaPlayerReward::getScorePoints).sum();
	}

	public boolean canRewarded() {
		return instance.getMapId() == 300350000 || instance.getMapId() == 300360000 || instance.getMapId() == 300550000 || instance.getMapId() == 300450000;
	}

	public int getNpcBonusSkill(int npcId) {
		switch (npcId) {
			case 701175: // Plaza Flame Thrower
			case 701176:
			case 701177:
			case 701178:
				return 0x4E5732; // 20055 50
			case 701189: // Plaza Flame Thrower
			case 701190:
			case 701191:
			case 701192:
				return 0x4FDB3C; // 20443 60
			case 701317: // Illusion of Hope level 47
				return 0x4f8532; // 20357 50
			case 701318: // Illusion of Hope level 51
				return 0x4f8537; // 20357 55
			case 701319: // Illusion of Hope level 56
				return 0x4f853C; // 20357 60
			case 701220: // Blesed Relic
				return 0x4E5537; // 20053 55 //20068, 20072
			case 207118:
			case 207119:
				return 0x50C101; // 20673 1
			case 207100:
				return 0x50BE01; // 20670 1
			default:
				return 0;
		}
	}

	public int getTime() {
		long result = System.currentTimeMillis() - instanceTime;
		if (isRewarded()) {
			return 0;
		}
		if (result < 120000) {
			return (int) (120000 - result);
		} else {
			return (int) (180000 * getRound() - (result - 120000));
		}
	}

	public void setInstanceStartTime() {
		this.instanceTime = System.currentTimeMillis();
	}

	public byte getBuffId() {
		return buffId;
	}

	@Override
	public void clear() {
		super.clear();
		positions.clear();
	}

}
