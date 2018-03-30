package com.aionemu.gameserver.model.instance.instancereward;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.IdgelDomePlayerInfo;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Ritsu, Estrayl
 */
public class IdgelDomeInfo extends InstanceReward<IdgelDomePlayerInfo> {

	public final static int WIN_AP = 5600;
	public final static int DEFEAT_AP = 1120;
	private final EnumMap<Race, WorldPosition> startPositions = new EnumMap<>(Race.class);
	private int asmodianPoints = 1000;
	private int elyosPoints = 1000;
	private int asmodianKills, elyosKills;

	public IdgelDomeInfo(int mapId, int instanceId) {
		super(mapId, instanceId);
		startPositions.put(Race.ELYOS, new WorldPosition(mapId, 269.76874f, 348.35953f, 79.44365f, (byte) 105));
		startPositions.put(Race.ASMODIANS, new WorldPosition(mapId, 259.3971f, 169.18243f, 79.430855f, (byte) 45));
	}

	public List<IdgelDomePlayerInfo> getPlayerRewardByRace(Race race) {
		return getInstanceRewards().stream().filter(r -> r.getRace() == race).collect(Collectors.toList());
	}

	public int getPointsByRace(Race race) {
		return race == Race.ELYOS ? elyosPoints : asmodianPoints;
	}

	public Race getWinningRace() {
		return elyosPoints >= asmodianPoints ? Race.ELYOS : Race.ASMODIANS;
	}

	public int getKillsByRace(Race race) {
		return race == Race.ELYOS ? elyosKills : asmodianKills;
	}

	public void addPointsByRace(Race race, int points) {
		switch (race) {
			case ELYOS:
				elyosPoints += points;
				break;
			case ASMODIANS:
				asmodianPoints += points;
				break;
		}
		if (elyosPoints < 0)
			elyosPoints = 0;
		if (asmodianPoints < 0)
			asmodianPoints = 0;
	}

	public void incrementKillsByRace(Race race) {
		switch (race) {
			case ELYOS:
				elyosKills++;
				break;
			case ASMODIANS:
				asmodianKills++;
				break;
		}
	}

	public int getAsmodianKills() {
		return asmodianKills;
	}

	public int getElyosKills() {
		return elyosKills;
	}

	public int getAsmodianPoints() {
		return asmodianPoints;
	}

	public int getElyosPoints() {
		return elyosPoints;
	}

	public void teleportToStartPosition(Player player) {
		teleport(player, startPositions.get(player.getRace()));
	}

	public void teleport(Player player, WorldPosition p) {
		TeleportService.teleportTo(player, p.getMapId(), instanceId, p.getX(), p.getY(), p.getZ(), p.getHeading());
	}
}
