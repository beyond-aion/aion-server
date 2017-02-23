package com.aionemu.gameserver.model.instance.instancereward;

import org.apache.commons.lang3.mutable.MutableInt;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.IdgelDomePlayerReward;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Ritsu
 */
public class IdgelDomeReward extends InstanceReward<IdgelDomePlayerReward> {

	private MutableInt asmodiansPoints = new MutableInt(0), elyosPoints = new MutableInt(0);
	private MutableInt asmodiansKills = new MutableInt(0), elyosKills = new MutableInt(0);
	private final static float[][] positions;
	private int racePosition;
	public final static int winningPoints = 5600;
	public final static int looserPoints = 1120;

	static {
		positions = new float[2][];
		positions[1] = new float[] { 259.3971f, 169.18243f, 79.430855f, 45 }; // asmodians
		positions[0] = new float[] { 269.76874f, 348.35953f, 79.44365f, 105 }; // elyos
	}

	public IdgelDomeReward(Integer mapId, int instanceId) {
		super(mapId, instanceId);
	}

	public IdgelDomePlayerReward[] getPlayersByRace(Race race) {
		int index = 0;
		IdgelDomePlayerReward[] players = new IdgelDomePlayerReward[12];
		for (IdgelDomePlayerReward reward : instanceRewards) {
			if (reward.getRace().equals(race)) {
				players[index] = reward;
				index++;
			}
		}
		return players;
	}

	public MutableInt getPointsByRace(Race race) {
		switch (race) {
			case ELYOS:
				return elyosPoints;
			case ASMODIANS:
				return asmodiansPoints;
		}
		return null;
	}

	public Race getWinningRace() {
		return elyosPoints.intValue() >= asmodiansPoints.intValue() ? Race.ELYOS : Race.ASMODIANS;
	}

	public MutableInt getKillsByRace(Race race) {
		switch (race) {
			case ELYOS:
				return elyosKills;
			case ASMODIANS:
				return asmodiansKills;
		}
		return null;
	}

	public void addPointsByRace(Race race, int points) {
		MutableInt racePoints = getPointsByRace(race);
		racePoints.add(points);
		if (racePoints.intValue() < 0) {
			racePoints.setValue(0);
		}
	}

	public MutableInt getAsmodiansKills() {
		return asmodiansKills;
	}

	public MutableInt getElyosKills() {
		return elyosKills;
	}

	public MutableInt getAsmodiansPoint() {
		return asmodiansPoints;
	}

	public MutableInt getElyosPoints() {
		return elyosPoints;
	}

	public int getRacePosition() {
		return racePosition;
	}

	public void portToPosition(Player player) {
		switch (player.getRace()) {
			case ELYOS:
				teleport(player, positions[0]);
				break;
			case ASMODIANS:
				teleport(player, positions[1]);
				break;
		}
	}

	public void teleport(Player player, float[] coordinates) {
		TeleportService.teleportTo(player, mapId, instanceId, coordinates[0], coordinates[1], coordinates[2], (byte) coordinates[3]);
	}

}
