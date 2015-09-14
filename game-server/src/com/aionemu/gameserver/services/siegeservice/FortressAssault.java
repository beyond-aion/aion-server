package com.aionemu.gameserver.services.siegeservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.AssaultType;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.assaults.AssaultSpawn;
import com.aionemu.gameserver.model.templates.spawns.assaults.AssaultWave;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Luzien
 * @reworked Whoop
 */
public class FortressAssault extends Assault<FortressSiege> {

	private static final Logger log = LoggerFactory.getLogger(FortressAssault.class);

	private boolean spawned = false;

	public FortressAssault(FortressSiege siege) {
		super(siege);
	}

	@Override
	protected void scheduleAssault(int delay) {
		dredgionTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				BalaurAssaultService.getInstance().spawnDredgion(getSpawnIdByFortressId());
				spawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						scheduleSpawns();
					}
				}, Rnd.get(240, 300) * 1000);
			}
		}, delay * 1000);
	}

	@Override
	protected void onAssaultFinish(boolean captured) {
		if (!spawned)
			return;

		if (captured) {
			siegeLocation.doOnAllPlayers(new Visitor<Player>() {

				@Override
				public void visit(Player player) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ABYSS_DRAGON_BOSS_KILLED);
				}
			});
		}
	}

	private void scheduleSpawns() {

		if (spawned)
			return;

		spawned = true;

		if (DataManager.SPAWNS_DATA2.getAssaultSpawnBySiegeId(locationId).getAssaultWaves().isEmpty())
			return;

		initiateSpawn(getWave(AssaultType.TELEPORT));
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (siegeLocation.isVulnerable()) {
					initiateSpawn(getWave(AssaultType.FIRST_WAVE));
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (siegeLocation.isVulnerable()) {
								initiateSpawn(getWave(AssaultType.SECOND_WAVE));
								ThreadPoolManager.getInstance().schedule(new Runnable() {

									@Override
									public void run() {
										if (siegeLocation.isVulnerable())
											initiateSpawn(getWave(AssaultType.COMMANDER));
									}
								}, Rnd.get(90, 180) * 1000);
							}
						}
					}, Rnd.get(90, 120) * 1000);
				}
			}
		}, Rnd.get(30, 60) * 1000);
	}

	private void initiateSpawn(AssaultWave wave) {
		int influenceMultiplier = getInfluenceMultiplier(siegeLocation.getRace());

		for (Spawn spawn : wave.getSpawns()) {
			for (SpawnSpotTemplate sst : spawn.getSpawnSpotTemplates()) {
				for (int i = 0; i < influenceMultiplier; i++) {
					spawnAssaulter(wave.getWorldId(), spawn.getNpcId(), locationId, sst.getX(), sst.getY(), sst.getZ(), (byte) 0, wave.getAssaultType());
				}
			}
		}
		if (wave.getAssaultType() == AssaultType.COMMANDER) {
			for (int i = 0; i < influenceMultiplier; i++) {
				spawnAssaulter(wave.getWorldId(), getCommanderIdByFortressId(), locationId, boss.getX(), boss.getY(), boss.getZ(), (byte) 0,
					wave.getAssaultType());
			}
		}
		announceInvasion(wave.getAssaultType());
	}

	private void spawnAssaulter(int mapId, int npcId, int locId, float x, float y, float z, byte heading, AssaultType aType) {

		float x1 = (float) (x + Math.cos(Math.PI * Rnd.get()) * Rnd.get(1, 3));
		float y1 = (float) (y + Math.sin(Math.PI * Rnd.get()) * Rnd.get(1, 3));

		SpawnTemplate spawnTemplate = SpawnEngine.addNewSiegeSpawn(mapId, npcId, locId, SiegeRace.BALAUR, SiegeModType.ASSAULT, x1, y1, z, heading);
		Npc invader = (Npc) SpawnEngine.spawnObject(spawnTemplate, 1);
		if (SiegeConfig.SIEGE_HEALTH_MOD_ENABLED && invader != null) {
			NpcTemplate templ = invader.getObjectTemplate();
			if (templ.getRating().equals(NpcRating.LEGENDARY)) {
				NpcLifeStats life = invader.getLifeStats();
				int maxHpPercent = (int) (life.getMaxHp() * SiegeConfig.SIEGE_HEALTH_MULTIPLIER);
				templ.getStatsTemplate().setMaxHp(maxHpPercent);
				life.setCurrentHpPercent(100);
			}
		}
		if (aType != AssaultType.TELEPORT)
			invader.getAggroList().addHate(boss, 100000);
	}

	private void announceInvasion(AssaultType aType) {
		siegeLocation.doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				switch (aType) {
					case TELEPORT:
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ABYSS_WARP_DRAGON);
						break;
					case FIRST_WAVE:
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FIELDABYSS_CARRIER_DROP_DRAGON);
						break;
					case SECOND_WAVE:
					case COMMANDER:
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ABYSS_CARRIER_DROP_DRAGON);
						break;
				}
			}
		});
	}

	private AssaultWave getWave(AssaultType aType) {
		AssaultWave wave = null;
		AssaultSpawn spawn = DataManager.SPAWNS_DATA2.getAssaultSpawnBySiegeId(locationId);
		if (spawn == null) {
			log.info("There are no assault spawns for siege " + locationId + " and wave " + aType);
			return wave;
		}
		for (AssaultWave awave : spawn.getAssaultWaves()) {
			if (awave.getAssaultType() == aType) {
				wave = awave;
				break;
			}
		}
		if (wave == null) {
			log.info("There is no assault wave for siege " + locationId + " and wave " + aType);
			return wave;
		}
		return wave;
	}

	private int getInfluenceMultiplier(SiegeRace defender) { // TODO: Maybe more dynamical?
		float influence;

		if (defender == SiegeRace.ASMODIANS)
			influence = Influence.getInstance().getGlobalAsmodiansInfluence();
		else
			influence = Influence.getInstance().getGlobalElyosInfluence();

		if (influence >= 0.9f)
			return 3;
		else if (influence >= 0.7f)
			return 2;
		else
			return 1;
	}

	private int getCommanderIdByFortressId() {
		switch (locationId) {
			case 1131: // Lower Abyss
			case 1132:
			case 1141:
				return 276649; // Lv40
			case 1211: // Outer Abyss
			case 1251:
				return 276871; // Lv50
			case 1011: // Divine
				return 882276;
			case 1221: // Inner Upper Abyss
			case 1231:
			case 1241:
				return 251385;
			case 2011: // Inggison | Gelkmaros
			case 2021:
			case 3011:
			case 3021:
				return 258236;
			case 5011: // Katalam
				return 272295;
			case 6011:
				return 272795;
			case 6021:
				return 273343;
			default:
				return 258236;
		}
	}

	private int getSpawnIdByFortressId() {
		switch (locationId) {
			case 2011:
				return 5;
			case 2021:
				return 6;
			case 3021:
				return 10;
			case 3011:
				return 11;
			case 1141:
				return 12;
			case 1221:
				return 13;
			case 1131:
				return 15;
			case 1132:
				return 14;
			case 1241:
				return 16;
			case 1231:
				return 17;
			case 1211:
				return 18;
			case 1251:
				return 19;
			case 1011:
				return 20;
			case 5011:
				return 23;
			case 6011:
				return 21;
			case 6021:
				return 22;
				// TODO: recheck 4.0
			default:
				return 1;
		}
	}
}
