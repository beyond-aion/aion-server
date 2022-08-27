package com.aionemu.gameserver.model.instance.instancereward;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.EngulfedOphidianBridgePlayerReward;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Tibald
 */
public class EngulfedOphidianBridgeReward extends AbstractInstancePointsAndKillsReward<EngulfedOphidianBridgePlayerReward> {

	private final int raceStartPosition;

	public EngulfedOphidianBridgeReward() {
		super(5600, 1120);
		raceStartPosition = Rnd.get(2);
	}

	public EngulfedOphidianBridgePlayerReward[] getPlayersByRace(Race race) {
		int index = 0;
		EngulfedOphidianBridgePlayerReward[] players = new EngulfedOphidianBridgePlayerReward[12];
		for (EngulfedOphidianBridgePlayerReward reward : getInstanceRewards()) {
			if (reward.getRace() == race) {
				players[index] = reward;
				index++;
			}
		}
		return players;
	}

	public int getRaceStartPosition() {
		return raceStartPosition;
	}

	public void portToPosition(Player player, WorldMapInstance instance) {
		if (player.getRace() == Race.ELYOS && raceStartPosition == 0 || player.getRace() == Race.ASMODIANS && raceStartPosition != 0) {
			TeleportService.teleportTo(player, instance,  762.01733f, 582.25903f, 578.2209f, (byte) 86);
		} else {
			TeleportService.teleportTo(player, instance,  294.72534f, 488.29645f, 598.5838f, (byte) 1);
		}
	}
}
