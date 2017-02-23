package com.aionemu.gameserver.model.instance.instancereward;

import org.apache.commons.lang3.mutable.MutableInt;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.KamarPlayerReward;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceGroup;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author xTz
 */
public class KamarReward extends InstanceReward<KamarPlayerReward> {

	private MutableInt asmodiansPoints = new MutableInt(3600), elyosPoints = new MutableInt(3600);
	private MutableInt asmodiansKills = new MutableInt(0), elyosKills = new MutableInt(0);
	private final static float[][] positions;
	private int elyosPosition = 0;
	public final static int winningPoints = 8000;
	public final static int losserPoints = 760;

	static {
		positions = new float[4][];
		positions[0] = new float[] { 1535.6466f, 1573.8773f, 612.4217f };
		positions[1] = new float[] { 1099.0986f, 1541.5055f, 585.0f };
		positions[2] = new float[] { 1204.9689f, 1350.8196f, 612.91205f };
		positions[3] = new float[] { 1446.6449f, 1232.9314f, 585.0623f };
	}

	public KamarReward(Integer mapId, int instanceId) {
		super(mapId, instanceId);
		elyosPosition = Rnd.get(2);

	}

	public KamarPlayerReward[] getPlayersByRace(Race race) {
		int index = 0;
		KamarPlayerReward[] players = new KamarPlayerReward[24];
		for (KamarPlayerReward reward : instanceRewards) {
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

	public void portToPosition(Player player) {
		PlayerAllianceGroup secGroup = null;
		if (player.isInAlliance()) {
			secGroup = player.getPlayerAlliance().getAllianceGroup(1001);
		}
		switch (player.getRace()) {
			case ELYOS:
				if (elyosPosition == 0) {
					if (!isReinforcing() && secGroup != null && secGroup.equals(player.getPlayerAllianceGroup())) {
						teleport(player, positions[1]);
					} else {
						teleport(player, positions[0]);
					}
				} else {
					if (!isReinforcing() && secGroup != null && secGroup.equals(player.getPlayerAllianceGroup())) {
						teleport(player, positions[3]);
					} else {
						teleport(player, positions[2]);
					}
				}
				break;
			case ASMODIANS:
				if (elyosPosition == 0) {
					if (!isReinforcing() && secGroup != null && secGroup.equals(player.getPlayerAllianceGroup())) {
						teleport(player, positions[3]);
					} else {
						teleport(player, positions[2]);
					}
				} else {
					if (!isReinforcing() && secGroup != null && secGroup.equals(player.getPlayerAllianceGroup())) {
						teleport(player, positions[1]);
					} else {
						teleport(player, positions[0]);
					}
				}
				break;
		}
	}

	public void teleport(Player player, float[] coordinates) {
		TeleportService.teleportTo(player, mapId, instanceId, coordinates[0], coordinates[1], coordinates[2]);
	}

}
