package com.aionemu.gameserver.services.instance;

import static com.aionemu.gameserver.configs.main.InstanceConfig.*;

import java.util.List;

import com.aionemu.gameserver.configs.main.InstanceConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Boidlz
 */
public class InstanceScaler implements StatOwner {

	private static final InstanceScaler INSTANCE = new InstanceScaler();

	public static void onEnterInstance(Player player) {
		if (!InstanceConfig.INSTANCE_SCALING_ENABLE)
			return;
		if (player.isStaff())
			return;
		WorldMapInstance instance = player.getPosition().getWorldMapInstance();
		if (instance.getMaxPlayers() <= 1)
			return;
		if (InstanceConfig.INSTANCE_SCALING_EXCLUDED_MAPS.contains(instance.getMapId()))
			return;
		rescale(instance, activePlayerCount(instance));
	}

	public static void onBeforeSpawn(Npc npc) {
		if (!InstanceConfig.INSTANCE_SCALING_ENABLE)
			return;
		WorldMapInstance instance = npc.getPosition().getWorldMapInstance();
		if (instance.getMaxPlayers() <= 1)
			return;
		if (InstanceConfig.INSTANCE_SCALING_EXCLUDED_MAPS.contains(instance.getMapId()))
			return;
		if (shouldScale(npc, instance)) {
			int count = activePlayerCount(instance);
			scaleNpc(npc, calculateMultiplier(instance, count, INSTANCE_SCALING_HP_FLOOR),
				calculateMultiplier(instance, count, INSTANCE_SCALING_DMG_FLOOR));
		}
	}

	private static void rescale(WorldMapInstance instance, int playerCount) {
		if (instance.getMaxPlayers() <= 1)
			return;
		if (playerCount == 0)
			return;
		double hpMulti = calculateMultiplier(instance, playerCount, INSTANCE_SCALING_HP_FLOOR);
		double dmgMulti = calculateMultiplier(instance, playerCount, INSTANCE_SCALING_DMG_FLOOR);
		for (Npc npc : instance.getNpcs())
			if (shouldScale(npc, instance))
				scaleNpc(npc, hpMulti, dmgMulti);			
	}

	private static boolean shouldScale(Npc npc, WorldMapInstance instance) {
		return instance.getPlayersInside().stream().filter(p -> !p.isStaff()).findFirst().map(npc::isEnemyFrom).orElse(false);
	}

	private static void scaleNpc(Npc npc, double hpMulti, double dmgMulti) {
		if (npc.isDead())
			return;
		npc.getGameStats().endEffect(INSTANCE);
		var stats = npc.getObjectTemplate().getStatsTemplate();
		int baseHp = stats.getMaxHp();
		int baseAtk = stats.getAttack();
		int baseMAtk = stats.getMagicalAttack();
		npc.getGameStats().addEffect(INSTANCE, List.of(new StatAddFunction(StatEnum.MAXHP, (int) (baseHp * hpMulti) - baseHp, true),
			new StatAddFunction(StatEnum.PHYSICAL_ATTACK, (int) (baseAtk * dmgMulti) - baseAtk, true),
			new StatAddFunction(StatEnum.MAGICAL_ATTACK, (int) (baseMAtk * dmgMulti) - baseMAtk, true)));
	}

	private static int activePlayerCount(WorldMapInstance instance) {
		return (int) instance.getPlayersInside().stream().filter(p -> !p.isStaff()).count();
	}

	private static double calculateMultiplier(WorldMapInstance instance, int playerCount, float floor) {
		return Math.max(floor, (double) Math.min(playerCount, instance.getMaxPlayers()) / instance.getMaxPlayers());
	}
}
