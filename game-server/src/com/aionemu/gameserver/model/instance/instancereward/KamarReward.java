package com.aionemu.gameserver.model.instance.instancereward;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.KamarPlayerReward;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class KamarReward extends AbstractInstancePointsAndKillsReward<KamarPlayerReward> {

	private final static float[][] positions = {
			{ 1535.6466f, 1573.8773f, 612.4217f },
			{ 1099.0986f, 1541.5055f, 585.0f },
			{ 1204.9689f, 1350.8196f, 612.91205f },
			{ 1446.6449f, 1232.9314f, 585.0623f }
	};
	private final int raceStartPosition;

	public KamarReward() {
		super(8000, 760);
		raceStartPosition = Rnd.get(2);
	}

	public KamarPlayerReward[] getPlayersByRace(Race race) {
		int index = 0;
		KamarPlayerReward[] players = new KamarPlayerReward[24];
		for (KamarPlayerReward reward : getInstanceRewards()) {
			if (reward.getRace() == race) {
				players[index] = reward;
				index++;
			}
		}
		return players;
	}

	public void portToPosition(Player player, WorldMapInstance instance) {
		boolean isSecondGroup = player.isInAlliance() && player.getPlayerAllianceGroup().getObjectId() == 1001;
		boolean useAlternativePos = !isReinforcing() && isSecondGroup;
		if (player.getRace() == Race.ELYOS && raceStartPosition == 0 || player.getRace() == Race.ASMODIANS && raceStartPosition != 0) {
				teleport(player, instance, useAlternativePos ? positions[1] : positions[0]);
		} else {
				teleport(player, instance, useAlternativePos ? positions[3] : positions[2]);
		}
	}

	public void teleport(Player player, WorldMapInstance instance, float[] coordinates) {
		TeleportService.teleportTo(player, instance, coordinates[0], coordinates[1], coordinates[2]);
	}
}
