package com.aionemu.gameserver.model.instance.instancescore;

import java.util.*;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instanceposition.*;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author xTz
 */
public class PvPArenaScore extends InstanceScore<PvPArenaPlayerReward> {

	private Map<Integer, Boolean> positions = new HashMap<>();
	private List<Integer> zones = new ArrayList<>();
	private int round = 1;
	private Integer zone;
	private int bonusTime;
	private int difficultyId;
	private int lowerScoreCap;
	private int upperScoreCap;
	private int maxScoreGap;
	private long instanceTime;
	private final byte buffId;
	protected WorldMapInstance instance;
	private GeneralInstancePosition instancePosition;

	public PvPArenaScore(WorldMapInstance instance) {
		this.instance = instance;

		WorldMapType wmt = WorldMapType.getWorld(instance.getMapId());
		boolean isSolo = isSoloArena(wmt);
		buffId = (byte) (isSolo ? 8 : 7);
		Collections.addAll(zones, isSolo ? new Integer[] { 1, 2, 3, 4 } : new Integer[] { 1, 2, 3, 4, 5, 6 });

		int positionSize = 0;
		switch (wmt) {
			case DISCIPLINE_TRAINING_GROUNDS, ARENA_OF_DISCIPLINE -> {
				bonusTime = 8100;
				positionSize = 4;
				instancePosition = new DisciplineInstancePosition();
			}
			case ARENA_OF_GLORY -> {
				bonusTime = 10800;
				positionSize = 8;
				instancePosition = new GloryInstancePosition();
			}
			case HARMONY_TRAINING_GROUNDS, ARENA_OF_HARMONY -> {
				bonusTime = 12000;
				positionSize = 12;
				instancePosition = new HarmonyInstancePosition();
			}
			case CHAOS_TRAINING_GROUNDS, ARENA_OF_CHAOS -> {
				bonusTime = 12000;
				positionSize = 12;
				instancePosition = new ChaosInstancePosition();
			}
			case null, default -> {
				return;
			}
		}

		instancePosition.initialize(instance.getMapId(), instance.getInstanceId());
		for (int i = 1; i <= positionSize; i++) {
			positions.put(i, Boolean.FALSE);
		}
		setRndZone();
	}

	public final boolean isSoloArena(WorldMapType wmt) {
		return wmt == WorldMapType.DISCIPLINE_TRAINING_GROUNDS || wmt == WorldMapType.ARENA_OF_DISCIPLINE;
	}

	public final void setRndZone() {
		int index = Rnd.nextInt(zones.size());
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

	public void incrementRound() {
		this.round++;
		if (round > 3)
			round = 3;
	}

	public boolean regPlayerReward(Player player) {
		if (!containsPlayer(player.getObjectId())) {
			addPlayerReward(new PvPArenaPlayerReward(player, bonusTime, buffId));
			return true;
		}
		return false;
	}

	public void portToPosition(Player player) {
		int objectId = player.getObjectId();
		regPlayerReward(player);
		setRndPosition(objectId);
		instancePosition.port(player, zone, getPlayerReward(objectId).getPosition());
	}

	public int getRank(PvPArenaPlayerReward reward) {
		List<PvPArenaPlayerReward> sortedByPoints = getPlayerRewards().stream().sorted(Comparator.comparing(PvPArenaPlayerReward::getScorePoints))
			.toList();

		int rank = -1;
		for (PvPArenaPlayerReward r : sortedByPoints) {
			if (r.getScorePoints() >= reward.getScorePoints())
				rank++;
		}
		return rank;
	}

	public boolean reachedScoreGap() {
		IntSummaryStatistics points = getPlayerRewards().stream().mapToInt(InstancePlayerReward::getPoints).summaryStatistics();
		int maxPoints = points.getMax();
		int minPoints = points.getMin();
		return maxPoints - minPoints >= maxScoreGap;
	}

	public int getTotalPoints() {
		return getPlayerRewards().stream().mapToInt(PvPArenaPlayerReward::getScorePoints).sum();
	}

	public boolean canReward() {
		return switch (WorldMapType.getWorld(instance.getMapId())) {
			case ARENA_OF_DISCIPLINE, ARENA_OF_GLORY, ARENA_OF_CHAOS, ARENA_OF_HARMONY -> true;
			case null, default -> false;
		};
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

	public int getDifficultyId() {
		return difficultyId;
	}

	public void setDifficultyId(int difficultyId) {
		this.difficultyId = difficultyId;
	}

	public int getLowerScoreCap() {
		return lowerScoreCap;
	}

	public void setLowerScoreCap(int lowerScoreCap) {
		this.lowerScoreCap = lowerScoreCap;
	}

	public int getUpperScoreCap() {
		return upperScoreCap;
	}

	public void setUpperScoreCap(int upperScoreCap) {
		this.upperScoreCap = upperScoreCap;
	}

	public void setMaxScoreGap(int maxScoreGap) {
		this.maxScoreGap = maxScoreGap;
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
