package com.aionemu.gameserver.services.instance;

import static com.aionemu.gameserver.configs.main.InstanceConfig.*;

import java.util.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.stats.CalculationType;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Minzi90
 */
public class InstanceScaler implements StatOwner {

	private static final InstanceScaler INSTANCE = new InstanceScaler();
	private static final Map<WorldMapInstance, Scaling> scalings = Collections.synchronizedMap(new WeakHashMap<>());

	private InstanceScaler() {
	}

	public static void onEnterInstance(Player player) {
		WorldMapInstance instance = player.getPosition().getWorldMapInstance();
		if (!canScale(instance))
			return;
		Scaling scaling = scalings.computeIfAbsent(instance, _ -> new Scaling());
		synchronized (scaling) {
			if (scaling.update(instance))
				rescale(instance, scaling);
		}
	}

	public static void onBeforeSpawn(Npc npc) {
		WorldMapInstance instance = npc.getPosition().getWorldMapInstance();
		if (!canScale(instance))
			return;
		Scaling scaling = scalings.get(instance);
		if (scaling == null)
			return;
		synchronized (scaling) {
			if (shouldScale(npc, instance))
				scaleNpc(npc, scaling);
		}
	}

	private static void rescale(WorldMapInstance instance, Scaling scaling) {
		for (Npc npc : instance.getNpcs())
			if (shouldScale(npc, instance))
				scaleNpc(npc, scaling);
	}

	public static boolean canScale(WorldMapInstance instance) {
		return INSTANCE_SCALING_ENABLE && instance.getMaxPlayers() > 1 && instance.getParent().isInstanceType() && !INSTANCE_SCALING_EXCLUDED_MAPS.contains(instance.getMapId());
	}

	private static boolean shouldScale(Npc npc, WorldMapInstance instance) {
		return !npc.isDead() && instance.getPlayersInside().stream().filter(p -> !p.isStaff()).findFirst().map(npc::isEnemyFrom).orElse(false);
	}

	private static void scaleNpc(Npc npc, Scaling scaling) {
		npc.getGameStats().endEffect(INSTANCE);
		if (!scaling.statFunctions.isEmpty())
			npc.getGameStats().addEffect(INSTANCE, scaling.statFunctions);
	}

	public static float calculateMultiplier(WorldMapInstance instance, float floor, int playerCount) {
		return Math.max(floor, (float) Math.min(playerCount, instance.getMaxPlayers()) / instance.getMaxPlayers());
	}

	static class Scaling {

		private int playerCount;
		private List<InstanceScalerStatFunction> statFunctions = Collections.emptyList();

		boolean update(WorldMapInstance instance) {
			int playerCount = (int) instance.getPlayersInside().stream().filter(p -> !p.isStaff()).count();
			if (this.playerCount >= playerCount)
				return false;
			this.playerCount = playerCount;
			this.statFunctions = createStatFunctions(instance, playerCount);
			return true;
		}

		private List<InstanceScalerStatFunction> createStatFunctions(WorldMapInstance instance, int playerCount) {
			List<InstanceScalerStatFunction> statFunctions = new ArrayList<>();
			float hpMulti = calculateMultiplier(instance, INSTANCE_SCALING_HP_FLOOR, playerCount);
			float dmgMulti = calculateMultiplier(instance, INSTANCE_SCALING_DMG_FLOOR, playerCount);
			if (hpMulti != 1) {
				statFunctions.add(new InstanceScalerStatFunction(StatEnum.MAXHP, hpMulti));
			}
			if (dmgMulti != 1) {
				statFunctions.add(new InstanceScalerStatFunction(StatEnum.PHYSICAL_ATTACK, dmgMulti));
				statFunctions.add(new InstanceScalerStatFunction(StatEnum.MAGICAL_ATTACK, dmgMulti));
				statFunctions.add(new InstanceScalerStatFunction(StatEnum.BOOST_SPELL_ATTACK, dmgMulti));
			}
			return statFunctions;
		}
	}

	static class InstanceScalerStatFunction extends StatFunction {

		private final float rate;

		InstanceScalerStatFunction(StatEnum stat, float rate) {
			this.stat = stat;
			this.rate = rate;
		}

		@Override
		public void apply(Stat2 stat, CalculationType... calculationTypes) {
			stat.setBaseRate(stat.getBaseRate() * rate);
			stat.setBonusRate(stat.getBonusRate() * rate);
		}

		@Override
		public int getPriority() {
			return 120;
		}
	}
}
