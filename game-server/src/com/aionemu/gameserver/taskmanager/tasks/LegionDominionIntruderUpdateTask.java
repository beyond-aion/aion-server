package com.aionemu.gameserver.taskmanager.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CONQUEROR_PROTECTOR;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldMapInstance;

public class LegionDominionIntruderUpdateTask extends AbstractPeriodicTaskManager {

	private final Map<Integer, List<Player>> playerTerritoryMap = new ConcurrentHashMap<>();

	private LegionDominionIntruderUpdateTask() {
		super(4000);
	}

	public void addPlayer(int territoryId, Player player) {
		synchronized (playerTerritoryMap) {
			playerTerritoryMap.computeIfAbsent(territoryId, (id) -> new ArrayList<>()).add(player);
		}
	}

	public void removePlayer(int territoryId, Player player) {
		synchronized (playerTerritoryMap) {
			if (playerTerritoryMap.get(territoryId) != null) {
				List<Player> players = playerTerritoryMap.get(territoryId);
				players.remove(player);
				if (players.isEmpty())
					playerTerritoryMap.remove(territoryId);
			}
		}
	}

	@Override
	public void run() {
		if (playerTerritoryMap.isEmpty())
			return;
		for (int territoryId : playerTerritoryMap.keySet()) {
			List<Player> players;
			players = playerTerritoryMap.get(territoryId);
			if (players == null || players.isEmpty())
				continue;
			players = new ArrayList<>(players);
			Map<Integer, List<Player>> instancePlayers = new HashMap<>();
			for (Player player : players) {
				WorldMap worldMap = World.getInstance().getWorldMap(player.getPosition().getMapId());
				if (worldMap == null)
					continue;
				List<Player> intruders = instancePlayers.computeIfAbsent(player.getPosition().getInstanceId(), (worldMapInstanceId) -> {
					try {
						WorldMapInstance worldMapInstance = worldMap.getWorldMapInstanceById(worldMapInstanceId);
						return worldMapInstance.getPlayersInside().stream().filter(p -> p.getRace() == player.getOppositeRace())
							.filter(p -> ConquerorAndProtectorService.getInstance().getCPInfoForCurrentMap(p) != null).collect(Collectors.toList());
					} catch (IllegalArgumentException e) {
						log.error("Error getting intruders for territorial [mapId: {}, id: {}] due to an invalid instanceId: {}", worldMap.getMapId(),
							territoryId, worldMapInstanceId);
						return Collections.emptyList();
					}
				});
				if (!intruders.isEmpty())
					PacketSendUtility.sendPacket(player, new SM_CONQUEROR_PROTECTOR(intruders, false));
			}
		}
	}

	public static LegionDominionIntruderUpdateTask getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static final class SingletonHolder {

		private static final LegionDominionIntruderUpdateTask INSTANCE = new LegionDominionIntruderUpdateTask();
	}

}
