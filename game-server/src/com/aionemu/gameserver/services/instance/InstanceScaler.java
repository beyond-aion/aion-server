package com.aionemu.gameserver.services.instance;

import static com.aionemu.gameserver.configs.main.InstanceConfig.*;

import java.util.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatRateFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Boidlz
 */
public class InstanceScaler implements StatOwner {

	private static final InstanceScaler INSTANCE = new InstanceScaler();
	private static final Map<WorldMapInstance, Integer> maxPlayerCounts = Collections.synchronizedMap(new WeakHashMap<>());

	public static void onEnterInstance(Player player) {
		WorldMapInstance instance = player.getPosition().getWorldMapInstance();
		if (canScale(instance) && playerCountIncreased(instance))
			rescale(instance);
	}

	public static void onBeforeSpawn(Npc npc) {
		WorldMapInstance instance = npc.getPosition().getWorldMapInstance();
		if (!canScale(instance) || maxPlayerCounts.get(instance) == null)
			return;
		if (shouldScale(npc, instance))
			scaleNpc(npc, calculateMultiplier(instance, INSTANCE_SCALING_HP_FLOOR), calculateMultiplier(instance, INSTANCE_SCALING_DMG_FLOOR));
	}

	private static void rescale(WorldMapInstance instance) {
		float hpMulti = calculateMultiplier(instance, INSTANCE_SCALING_HP_FLOOR);
		float dmgMulti = calculateMultiplier(instance, INSTANCE_SCALING_DMG_FLOOR);
		for (Npc npc : instance.getNpcs())
			if (shouldScale(npc, instance))
				scaleNpc(npc, hpMulti, dmgMulti);
	}

	public static boolean canScale(WorldMapInstance instance) {
		return INSTANCE_SCALING_ENABLE && instance.getMaxPlayers() > 1 && instance.getParent().isInstanceType() && !INSTANCE_SCALING_EXCLUDED_MAPS.contains(instance.getMapId());
	}

	private static boolean shouldScale(Npc npc, WorldMapInstance instance) {
		return !npc.isDead() && instance.getPlayersInside().stream().filter(p -> !p.isStaff()).findFirst().map(npc::isEnemyFrom).orElse(false);
	}

	private static void scaleNpc(Npc npc, float hpMulti, float dmgMulti) {
		npc.getGameStats().endEffect(INSTANCE);
		var stats = npc.getObjectTemplate().getStatsTemplate();
		List<StatFunction> statFunctions = new ArrayList<>();
		if (hpMulti != 1) {
			int baseHp = stats.getMaxHp();
			statFunctions.add(new StatAddFunction(StatEnum.MAXHP, (int) (baseHp * hpMulti) - baseHp, true));
		}
		if (dmgMulti != 1) {
			int baseAtk = stats.getAttack();
			int baseMAtk = stats.getMagicalAttack();
			statFunctions.add(new StatAddFunction(StatEnum.PHYSICAL_ATTACK, (int) (baseAtk * dmgMulti) - baseAtk, true));
			statFunctions.add(new StatAddFunction(StatEnum.MAGICAL_ATTACK, (int) (baseMAtk * dmgMulti) - baseMAtk, true));
			statFunctions.add(new StatRateFunction(StatEnum.BOOST_SPELL_ATTACK, (int) (100 * dmgMulti - 100), true));
		}
		if (!statFunctions.isEmpty())
			npc.getGameStats().addEffect(INSTANCE, statFunctions);
	}

	private static boolean playerCountIncreased(WorldMapInstance instance) {
		int playerCount = (int) instance.getPlayersInside().stream().filter(p -> !p.isStaff()).count();
		int previousPlayerCount = maxPlayerCounts.getOrDefault(instance, 0);
		int maxPlayerCount = maxPlayerCounts.compute(instance, (_, v) -> v == null || v < playerCount ? playerCount : v);
		return previousPlayerCount < maxPlayerCount;
	}

	public static float calculateMultiplier(WorldMapInstance instance, float floor) {
		int playerCount = maxPlayerCounts.getOrDefault(instance, instance.getMaxPlayers());
		return Math.max(floor, (float) Math.min(playerCount, instance.getMaxPlayers()) / instance.getMaxPlayers());
	}
}
