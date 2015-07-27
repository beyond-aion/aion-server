package com.aionemu.gameserver.services.agentsfight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.callbacks.util.GlobalCallbackHelper;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.base.Base;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.google.common.collect.Lists;

import javolution.util.FastList;

/**
 * @author Yeats
 *
 */
public class AgentsFightService {

	private static final AgentsFightService instance = new AgentsFightService();
	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
	private boolean started = false;
	private Future<?> timer, adds, flags;
	private final AgentsFightCounter counter = new AgentsFightCounter();
	private final static AgentsFightAPListener apListener = new AgentsFightAPListener();
	private List<Npc> agents = new FastList<>();
	private List<Npc> spawnedNpcs = new FastList<>();
	
	public static AgentsFightService getInstance() {
		return instance;
	}
	
	private AgentsFightCounter getCounter() {
		return counter;
	}
	
	public void onStart() {
		boolean doubleStart = false;
		synchronized (this) {
			if (started) {
				doubleStart = true;
			} else {
				started = true;
			}
		}
		
		if (doubleStart) {
			if (LoggingConfig.LOG_SIEGE)
				log.error("[AGENTS FIGHT SERVICE] Attempt to start Agents Fight for the 2nd time.");
			return;
		}
		
		if (LoggingConfig.LOG_SIEGE)
			log.info("[AGENTS FIGHT SERVICE] Agents Fight started.");
		spawnAgents();
		spawnFlags();
		spawnAdds();
		sendMsg(1402543, 0); //Agents spawned
		startTimer();
		
	}

	private void spawnAgents() {
		/**
		 * TODO 
		 * temp. fix:
		 * Walking Npcs are buggy, therefore we'll spawn them near their destination and let them just walk 2 steps till its fixed!
		 */
		/*
		//Empowered Veille
		SpawnTemplate veille = SpawnEngine.addNewSingleTimeSpawn(600100000, 235064, 876.84f, 680.76f, 271.725f, (byte) 4);
		veille.setWalkerId("ldf4_advance_npcpathgod_l_4.2");
		SpawnEngine.spawnObject(veille, 1);
		
		//Empowered Mastarius
		SpawnTemplate masta = SpawnEngine.addNewSingleTimeSpawn(600100000, 235065, 1023.62f, 1397.43f, 264.95813f, (byte) 7);
		masta.setWalkerId("ldf4_advance_npcpathgod_d_4.4");
		SpawnEngine.spawnObject(masta, 1);
		*/
		
		/**
		 * Delete this when walk stuff is fixed.
		 */
	//Empowered Veille
		SpawnTemplate veille = SpawnEngine.addNewSingleTimeSpawn(600100000, 235064, 860.61f, 1071.74f, 332.6346f, (byte) 15);
		veille.setWalkerId("ldf4_advance_npcpathgod_l_4.2");
		agents.add((Npc) SpawnEngine.spawnObject(veille, 1));
		
		//Empowered Mastarius
		SpawnTemplate masta = SpawnEngine.addNewSingleTimeSpawn(600100000, 235065, 892.74f, 1110.34f, 332.6346f, (byte) 76);
		masta.setWalkerId("ldf4_advance_npcpathgod_d_4.4");
		agents.add((Npc) SpawnEngine.spawnObject(masta, 1));
	}
	
	private void spawnFlags() {
		flags = ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (started) {
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 832831, 912.44f, 1127.38f, 336.59f, (byte) 6), 1)); //Flag
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 832830, 855.63f, 1066.84f, 336.59f, (byte) 6), 1)); //Flag
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 702638, 934.88f, 1061.09f, 343.67f, (byte) 3), 1)); //Quest Npc
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 702637, 924.80f, 1042.08f, 343.67f, (byte) 3), 1)); //Timer Npc
					GlobalCallbackHelper.addCallback(apListener);
					for (Npc npc : agents) {
						if (npc != null && !npc.getLifeStats().isAlreadyDead())
							npc.getEffectController().removeEffect(21779);
					}
				}
			}
		}, /*(1000 * 60 * 5) - 15000*/ 20000);
	}
	
	private void spawnAdds() {
		adds = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (started) {
					//Veille Adds
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235334, 862.96f, 1094.75f, 333f, (byte) 113), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235335, 862.4f, 1090.33f, 333f, (byte) 2), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235336, 863.6f, 1084.9f, 333f, (byte) 9), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235337, 868f, 1089.5f, 333f, (byte) 21), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235338, 872.9f, 1077.2f, 333f, (byte) 27), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235339, 881f, 1089.1f, 333f, (byte) 38), 1));
				
					//Masta Adds
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235340, 888.6f, 1086.2f, 333f, (byte) 53), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235341, 889f, 1094.1f, 333f, (byte) 66), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235342, 885.44f, 1100f, 333f, (byte) 75), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235343, 881.3f, 1102.8f, 333f, (byte) 85), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235344, 875.3f, 1104f, 333f, (byte) 91), 1));
					spawnedNpcs.add((Npc) SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600100000, 235345, 869.5f, 1102.3f, 333f, (byte) 99), 1));
				}
			}
		}, /*(1000 * 60 * 5) + 40000*/ 60000);
	}
	
	private static void sendMsg(int id, int mapId) {
		if (mapId > 0) {
			World.getInstance().getWorldMap(mapId).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {
			
				@Override
				public void visit(Player player) {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(id));
				}
			});
		} else {
			World.getInstance().doOnAllPlayers(new Visitor<Player>() {
			
				@Override
				public void visit(Player player) {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(id));
				}
			});
		}
	}
	
	public void stop(NpcAI2 npcAI) {
		
		boolean doubleStop = false;
		synchronized (this) {
			if (started) {
					started = false;
			} else {
				doubleStop = true;
			}
		}
		if (doubleStop) {
			if (LoggingConfig.LOG_SIEGE)
				log.error("[AGENTS FIGHT SERVICE] Attempt to stop Agents Fight for the 2nd time.");
			return;
		}
		
		cancelTask();
		if (npcAI == null) {
			deleteNpc(235065);
			deleteNpc(235064);
			getCounter().clearAll();
			if (LoggingConfig.LOG_SIEGE)
				log.info("[AGENTS FIGHT SERVICE] Agents Fight finished. Time's up.");
		} else if (npcAI.getOwner().getRace() == Race.GHENCHMAN_LIGHT) {
			deleteNpc(235065);
			if (LoggingConfig.LOG_SIEGE)
				log.info("[AGENTS FIGHT SERVICE] Agents Fight finished. ASMODIANS won!");
		} else {
			deleteNpc(235064);
			if (LoggingConfig.LOG_SIEGE)
				log.info("[AGENTS FIGHT SERVICE] Agents Fight finished. ELYOS won!");
		}
		
		deleteNpc(0);
		
		GlobalCallbackHelper.removeCallback(apListener);
		spawnedNpcs.clear();
		agents.clear();
		if (npcAI == null) {
			return;
		} else if (npcAI.getOwner().getRace() == Race.GHENCHMAN_LIGHT) { //Asmodians win
			calculateSinglePlayersGloryPoints(getCounter().getRaceCounter(Race.ASMODIANS));
			@SuppressWarnings("rawtypes")
			Base base = BaseService.getInstance().getActiveBase(6113);
			if (base != null) {
				BaseService.getInstance().capture(base.getId(), Race.ASMODIANS);
			}
		} else { //Elyos win
			calculateSinglePlayersGloryPoints(getCounter().getRaceCounter(Race.ELYOS));
			@SuppressWarnings("rawtypes")
			Base base = BaseService.getInstance().getActiveBase(6113);
			if (base != null) {
				BaseService.getInstance().capture(base.getId(), Race.ELYOS);
			}
		}
	}
	
	private void startTimer() {
		timer = ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				stop(null);
			}
		}, /*1000 * 60 * 35 */ 1000 * 60 * 30 + 20000);
	}
	
	private void cancelTask() {
		if (timer != null && !timer.isCancelled()) {
			timer.cancel(true);
		}
		if (adds != null && !adds.isCancelled()) {
			adds.cancel(true);
		}
		if (flags != null && !flags.isCancelled()) {
			flags.cancel(true);
		}
	}

	private void deleteNpc(int id) {
		if (id == 0) { //delete all spawns except agents
			for (Npc npc : spawnedNpcs) {
				if (npc != null) {
					npc.getController().onDelete();
				}
			}
		} else { //delete agent with id
			for (Npc npc : agents) {
				if (npc != null && npc.getNpcId() == id) {
					npc.getController().delete();
				}
			}
		}
	}

	/**
	 * @param player
	 * @param abyssPoints
	 */
	public void addAP(Player player, int ap) {
		if (!started) {
			return;
		}
		if (player == null) {
			return;
		}
		getCounter().addAP(player, ap);
	}

	/**
	 * @param attacker
	 * @param damage
	 */
	public void addDamage(Creature attacker, int damage) {
		if (!started) {
			return;
		}
		if (attacker == null) {
			return;
		}
	
		attacker = attacker.getMaster();
		if (attacker instanceof Player) {
			getCounter().addDamage((Player) attacker, damage);
		}
	}
	
	private void calculateSinglePlayersGloryPoints(AgentsFightRaceCounter raceCounter) {
		float multi = SiegeConfig.AGENTS_FIGHT_AP_MULTI;
		long totalDmgAndAp = (long) (raceCounter.getTotalAp() * multi + raceCounter.getTotalDmg());
		if (totalDmgAndAp <= 0) {
			getCounter().clearAll();
			return;
		}
		Map<Integer, Long> playersApCounter = raceCounter.getPlayerAbyssPoints();
		Map<Integer, Long> playersDmgCounter = raceCounter.getPlayerDamage();
		List<Integer> allDmgPlayers = null;
		List<Integer> allApPlayers = null;
		if (playersDmgCounter != null && !playersDmgCounter.isEmpty()) {
			allDmgPlayers = Lists.newArrayList(playersDmgCounter.keySet());
		}
		if (playersApCounter != null && !playersApCounter.isEmpty()) {
			allApPlayers = Lists.newArrayList(playersApCounter.keySet());
		}
		
		List<Integer> allPlayers = new ArrayList<>(); 
		if (allDmgPlayers != null && !allDmgPlayers.isEmpty()) {
			for (Integer dmgPlayer : allDmgPlayers) {
				if (!allPlayers.contains(dmgPlayer)) {
					allPlayers.add(dmgPlayer);
				}	
			}
		}
		if (allApPlayers != null && !allApPlayers.isEmpty()) {
			for (Integer apPlayer : allApPlayers) {
				if (!allPlayers.contains(apPlayer)) {
					allPlayers.add(apPlayer);
				}
			}
		}
		
		if (allPlayers.isEmpty()) {
			getCounter().clearAll();
			return;
		}
		
		int maxGP = SiegeConfig.AGENTS_FIGHT_MAX_GP_REWARD;
		int maxSinglePlayerGp = SiegeConfig.AGENTS_FIGHT_PLAYER_GP_LIMIT;
		int rewardedPlayers = 0;
		for (Integer playerId : allPlayers) {
			long dmg = 0;
			long ap = 0;
			if (playersDmgCounter.containsKey(playerId)) {
				dmg = playersDmgCounter.get(playerId);
			}
			if (playersApCounter.containsKey(playerId)) {
				ap = playersApCounter.get(playerId);
			}
			
			if (ap <= 0 && dmg <= 0) {
				continue;
			}
			
			long totaldmg = (long) (ap * multi + dmg);
			float percentage = totaldmg / totalDmgAndAp;
			float rewardGp = maxGP * percentage;
			if (rewardGp > maxSinglePlayerGp) {
				rewardGp = maxSinglePlayerGp;
			}
			
			if (rewardGp > 0) {
				int reward = Math.round(rewardGp);
				GloryPointsService.increaseGp(playerId, reward);
				rewardedPlayers++;
				if (LoggingConfig.LOG_SIEGE)
					log.info("[AGENTS FIGHT SERVICE] REWARDED playerId: " + playerId + " WITH " + reward + " GLORY POINTS");
			}
		}
		getCounter().clearAll();
		if (LoggingConfig.LOG_SIEGE)
			log.info("[AGENTS FIGHT SERVICE] REWARDED A TOTAL OF " + rewardedPlayers +  " PLAYERS.");
	}
	
	public boolean isStarted() {
		return started;
	}
}
