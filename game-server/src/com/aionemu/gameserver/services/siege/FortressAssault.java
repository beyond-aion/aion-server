package com.aionemu.gameserver.services.siege;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.Assaulter;
import com.aionemu.gameserver.model.siege.AssaulterType;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.siegelocation.AssaultData;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_ASSEMBLER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Luzien, Estrayl
 *         TODO: Fortress gate, gate restoration stone and aetheric field destruction
 */
public class FortressAssault extends Assault<FortressSiege> {

	private final static Logger log = LoggerFactory.getLogger("SIEGE_LOG");
	private final List<Assaulter> commanderSpawnList = new ArrayList<>();
	private final AssaultData assaultData;
	private float difficulty, commanderSpawnChance, spawnBudget, startBudget;
	private int minSpawnDelay, waveCount, possibleCommanderCount;

	public FortressAssault(FortressSiege siege) {
		super(siege);
		assaultData = siegeLocation.getTemplate().getAssaultData();
		calculateDifficultySettings();
	}

	@Override
	protected void handleAssault() {
		BalaurAssaultService.getInstance().spawnDredgion(assaultData.getDredgionId());
		scheduleSpawns();
	}

	@Override
	protected void onAssaultFinish(boolean isCaptured) {
		if (isCaptured)
			announce(SM_SYSTEM_MESSAGE.STR_ABYSS_DRAGON_BOSS_KILLED(getBossNpcL10n()));
	}

	private void scheduleSpawns() {
		int delay = minSpawnDelay >= assaultData.getBaseDelay() ? minSpawnDelay : Rnd.get(minSpawnDelay, assaultData.getBaseDelay());
		spawnTask = ThreadPoolManager.getInstance().schedule(this::spawnWave, delay, TimeUnit.SECONDS);
	}

	private void spawnWave() {
		if (!siegeLocation.isVulnerable() || spawnBudget < 0.1f)
			return;

		switch (++waveCount) {
			case 1:
			case 10:
				List<Assaulter> teleportWave = assaultData.getProcessedAssaulters().get(AssaulterType.TELEPORT);
				for (SiegeNpc npc : World.getInstance().getLocalSiegeNpcs(locationId))
					if (npc.getRating() != NpcRating.LEGENDARY && npc.getAbyssNpcType() != AbyssNpcType.ARTIFACT && Rnd.chance() < 40)
						spawnAssaulter(Rnd.get(teleportWave), npc);
				announce(SM_SYSTEM_MESSAGE.STR_ABYSS_WARP_DRAGON());
				break;
			default:
				computeWave().forEach(a -> spawnAssaulter(a, boss));
				if (!commanderSpawnList.isEmpty()) {
					if (Rnd.chance() < commanderSpawnChance) {
						spawnAssaulter(commanderSpawnList.remove(0), boss);
						commanderSpawnChance = 0f;
					} else {
						commanderSpawnChance += 15 + 5 * difficulty;
					}
				}
				announce(SM_SYSTEM_MESSAGE.STR_ABYSS_CARRIER_DROP_DRAGON());
				break;
		}
		scheduleSpawns();
	}

	private List<Assaulter> computeWave() {
		List<Assaulter> finalList = new ArrayList<>();
		EnumMap<AssaulterType, List<Assaulter>> assaulterMap = assaultData.getProcessedAssaulters();
		for (AssaulterType type : AssaulterType.values()) {
			if (type == AssaulterType.TELEPORT || type == AssaulterType.COMMANDER)
				continue;
			addAssaulters(finalList, assaulterMap.get(type), spawnBudget * type.getSpawnStake());
		}
		return finalList;
	}

	private void addAssaulters(List<Assaulter> output, List<Assaulter> input, float budget) {
		if (input == null || input.isEmpty())
			return;
		while (budget > 0.0f) {
			float budgetCopy = budget;
			Assaulter a = Rnd.get(input.stream().filter(assaulter -> assaulter.getSpawnCost() <= budgetCopy).collect(Collectors.toList()));
			if (a == null) {
				a = input.get(0);
			}
			output.add(a);
			budget -= a.getSpawnCost();
		}
	}

	private void announce(SM_SYSTEM_MESSAGE msg) {
		siegeLocation.forEachPlayer(p -> PacketSendUtility.sendPacket(p, msg));
	}

	private void calculateDifficultySettings() {
		float factionBalance = getFactionBalanceMultiplier();
		float influence = getInfluenceMultiplier();

		difficulty = factionBalance / 3f * (1f + influence) * SiegeConfig.SIEGE_DIFFICULTY_MULTIPLIER;

		spawnBudget = Math.max(assaultData.getBaseBudget() / 3f, Math.round(assaultData.getBaseBudget() * difficulty));
		startBudget = spawnBudget;
		addAssaulters(commanderSpawnList, assaultData.getProcessedAssaulters().get(AssaulterType.COMMANDER), difficulty);
		possibleCommanderCount = commanderSpawnList.size();

		minSpawnDelay = Math.min(Math.round(assaultData.getBaseDelay() / difficulty), assaultData.getBaseDelay() - 10);
		if (minSpawnDelay < 30) // just in case SIEGE_DIFFICULTY_MULTIPLIER is set beyond 1.0 (100%)
			minSpawnDelay = 30;

		log.info("Initialized fortress assault on [locationID=" + locationId + "] with [difficulty=" + difficulty + "] [factionBalance=" + factionBalance
			+ "] [influence=" + influence + "] [difficultyMultiplier=" + SiegeConfig.SIEGE_DIFFICULTY_MULTIPLIER + "]");
	}

	private float getFactionBalanceMultiplier() {
		int factionBalance = siegeLocation.getFactionBalance();
		switch (siegeLocation.getRace()) {
			case ASMODIANS:
				if (factionBalance < 0)
					return Math.abs(factionBalance);
				break;
			case ELYOS:
				if (factionBalance > 0)
					return Math.abs(factionBalance);
				break;
		}
		return 1f;
	}

	private float getInfluenceMultiplier() {
		switch (siegeLocation.getRace()) {
			case ASMODIANS:
				return Influence.getInstance().getAsmodianInfluenceRate();
			case ELYOS:
				return Influence.getInstance().getElyosInfluenceRate();
			default:
				return 1f;
		}
	}

	public void onDredgionCommanderKilled() {
		spawnBudget -= startBudget * (1f / possibleCommanderCount);
		if (spawnBudget < 0.1f) {
			World.getInstance().forEachPlayer(p -> {
				PacketSendUtility.sendPacket(p, new SM_NPC_ASSEMBLER(null));
				PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_ABYSS_CARRIER_DESPAWN());
			});
			if (spawnTask != null)
				spawnTask.cancel(true);
			log.info("Finished fortress assault on [locationID=" + locationId + "] by defeating " + possibleCommanderCount + " dredgion commanders after "
				+ waveCount + " waves.");
		}
	}
}
