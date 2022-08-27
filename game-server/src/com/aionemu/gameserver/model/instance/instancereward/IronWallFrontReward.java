package com.aionemu.gameserver.model.instance.instancereward;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.IronWallFrontPlayerReward;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Tibald
 */
public class IronWallFrontReward extends AbstractInstancePointsAndKillsReward<IronWallFrontPlayerReward> {

	private final int elyosPosition;

	public IronWallFrontReward() {
		super(5600, 1120);
		elyosPosition = Rnd.get(2);
	}

	public IronWallFrontPlayerReward[] getPlayersByRace(Race race) {
		int index = 0;
		IronWallFrontPlayerReward[] players = new IronWallFrontPlayerReward[12];
		for (IronWallFrontPlayerReward reward : getInstanceRewards()) {
			if (reward.getRace() == race) {
				players[index] = reward;
				index++;
			}
		}
		return players;
	}

	public void portToPosition(Player player, WorldMapInstance instance) {
		if (player.getRace() == Race.ELYOS && elyosPosition == 0 || player.getRace() == Race.ASMODIANS && elyosPosition != 0) {
			TeleportService.teleportTo(player, instance,  274.143f, 384.335f, 239.973f, (byte) 14);
		} else {
			TeleportService.teleportTo(player, instance,  598.229f, 712.984f, 223.306f, (byte) 73);
		}
	}

}
