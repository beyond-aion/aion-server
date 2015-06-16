package com.aionemu.gameserver.model.instance.instancereward;

import org.apache.commons.lang3.mutable.MutableInt;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.EngulfedOphidianBridgePlayerReward;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 *
 * @author Tibald
 */
public class EngulfedOphidianBridgeReward extends InstanceReward<EngulfedOphidianBridgePlayerReward> {

	private MutableInt asmodiansPoints = new MutableInt(0), elyosPoints = new MutableInt(0);
	private MutableInt asmodiansKills = new MutableInt(0), elyosKills = new MutableInt(0);
	private final static float[][] positions;
	private int racePosition;
	public final static int winningPoints = 5600;
	public final static int looserPoints = 1120;

	static {
		positions = new float[2][];
		positions[1] = new float[]{294.72534f, 488.29645f, 598.5838f, 1}; // asmodians
		positions[0] = new float[]{762.01733f, 582.25903f, 578.2209f, 86}; // elyos
	}

	public EngulfedOphidianBridgeReward(Integer mapId, int instanceId) {
		super(mapId, instanceId);
		racePosition = Rnd.get(2);
	}

	public EngulfedOphidianBridgePlayerReward[] getPlayersByRace(Race race) {
		int index = 0;
		EngulfedOphidianBridgePlayerReward[] players = new EngulfedOphidianBridgePlayerReward[12];
		for (EngulfedOphidianBridgePlayerReward reward : instanceRewards) {
			if (reward.getRace().equals(race)) {
				players[index] = reward;
				index++;
			}
		}
		return players;
	}

	@Override
	public EngulfedOphidianBridgePlayerReward getPlayerReward(Integer object) {
		return (EngulfedOphidianBridgePlayerReward) super.getPlayerReward(object);
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
				if (racePosition == 0) {
					teleport(player, positions[0]);
				}
				else {
					teleport(player, positions[1]);
				}
				break;
			case ASMODIANS:
				if (racePosition == 0) {
					teleport(player, positions[1]);
				}
				else {
					teleport(player, positions[0]);
				}
				break;
		}
	}

	public void teleport(Player player, float[] coordinates) {
		TeleportService2.teleportTo(player, mapId, instanceId, coordinates[0], coordinates[1], coordinates[2], (byte) coordinates[3]);
	}

}
