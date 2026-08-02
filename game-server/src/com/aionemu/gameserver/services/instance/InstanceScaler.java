package com.aionemu.gameserver.services.instance;

import static com.aionemu.gameserver.configs.main.InstanceConfig.*;

import java.util.*;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.InstanceCooltime;
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
		return npc.getRating().ordinal() >= INSTANCE_SCALING_NPC_MIN_RATING.ordinal() && !npc.isDead() && instance.getPlayersInside().stream().filter(p -> !p.isStaff()).findFirst().map(npc::isEnemyFrom).orElse(false);
	}

	private static void scaleNpc(Npc npc, Scaling scaling) {
		npc.getGameStats().endEffect(INSTANCE);
		if (!scaling.statFunctions.isEmpty())
			npc.getGameStats().addEffect(INSTANCE, scaling.statFunctions);
	}

	public static float calculateMultiplier(WorldMapInstance instance, float floor, float scaleFactor, int playerCount) {
		float multi = (float) Math.min(playerCount, instance.getMaxPlayers()) / instance.getMaxPlayers();
		multi = 1 - (1 - multi) * scaleFactor;
		return Math.max(floor, multi);
	}

	static class Scaling {

		private int playerCount;
		private List<InstanceScalerStatFunction> statFunctions = Collections.emptyList();

		boolean update(WorldMapInstance instance) {
			List<Player> players = instance.getPlayersInside().stream().filter(p -> !p.isStaff()).toList();
			int playerCount = players.size();
			if (playerCount < instance.getMaxPlayers() && isLowLevelInstanceWithHighLevelPlayers(instance, players))
				playerCount = instance.getMaxPlayers(); // disable scaling
			if (this.playerCount >= playerCount)
				return false;
			this.playerCount = playerCount;
			this.statFunctions = createStatFunctions(instance, playerCount);
			return true;
		}

		private boolean isLowLevelInstanceWithHighLevelPlayers(WorldMapInstance instance, List<Player> players) {
			if (players.isEmpty())
				return false;
			int maxAllowedLevel = getInstanceEnterMinLevel(instance, players) + INSTANCE_SCALING_MAX_LEVEL_DIFF;
			return players.stream().mapToInt(Player::getLevel).max().orElseThrow() > maxAllowedLevel;
		}

		private int getInstanceEnterMinLevel(WorldMapInstance instance, List<Player> players) {
			InstanceCooltime ct = DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(instance.getMapId());
			return players.stream().mapToInt(p -> p.getRace() == Race.ASMODIANS ? ct.getEnterMinLevelDark() : ct.getEnterMinLevelLight()).min().orElse(1);
		}

		private List<InstanceScalerStatFunction> createStatFunctions(WorldMapInstance instance, int playerCount) {
			List<InstanceScalerStatFunction> statFunctions = new ArrayList<>();
			float hpMulti = calculateMultiplier(instance, INSTANCE_SCALING_HP_FLOOR, INSTANCE_SCALING_HP_SCALE_FACTOR, playerCount);
			float dmgMulti = calculateMultiplier(instance, INSTANCE_SCALING_DMG_FLOOR, INSTANCE_SCALING_DMG_SCALE_FACTOR, playerCount);
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
