package com.aionemu.gameserver.services.panesterra.ahserion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.knownlist.Visitor;

import javolution.util.FastMap;

/**
 * @author Yeats
 *
 */
public class AhserionInstance {

	private static final AhserionInstance instance = new AhserionInstance();
	private AhserionInstanceStatus status = AhserionInstanceStatus.OFF;
	private Map<PanesterraTeamId, PanesterraTeam> teams = null;
	private AhserionTeam winner = null;
	private Future<?> registrationTimer, instanceChecker, instanceTimer;
	private boolean started = false;
	
	public static AhserionInstance getInstance() {
		return instance;
	}
	
	public void start() {
		boolean doubleStart = false;
		synchronized (this) {
			if (started) {
				doubleStart = true;
			} else {
				started = true;
			}
		}
		if (doubleStart) {
			return;
		}
		status = AhserionInstanceStatus.PREPARING_REGISTRATION;
		PanesterraMatchmakingService.getInstance().onStart();
		startRegistrationTimer();
	}
	
	private void startRegistrationTimer() {
		registrationTimer = ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				startInstancePreparation();
			}
			
		}, 300000); //5min
	}
	
	private void startInstancePreparation() {
		synchronized (this) {
			status = AhserionInstanceStatus.PREPARING_INSTANCE_START;
		}
		PanesterraMatchmakingService.getInstance().prepareTeams();
		teams = PanesterraMatchmakingService.getInstance().getRegisteredTeams();
		if (teams != null && !teams.isEmpty()) {
			PanesterraMatchmakingService.getInstance().onStop();
			startInstance();
		} else {
			onStop();
		}
	}
	
	private void startInstance() {
		//move players to the instance and start instance timer
		spawnInstance();
		teleportPlayers();
		instanceStartTimer();
	}
	
	private void spawnInstance() {
		//spawn Barricades & Tank Fleets
		spawnStage(0, PanesterraTeamId.BALAUR);
		spawnStage(180, PanesterraTeamId.BALAUR);
		spawnStage(181, PanesterraTeamId.BALAUR);
		spawnStage(182, PanesterraTeamId.BALAUR);
		spawnStage(183, PanesterraTeamId.BALAUR);
		
		//spawn flags & cannons for all registered teams
		for (PanesterraTeamId teamId : teams.keySet()) {
			if (teamId != null )
			spawnStage(0, teamId);
			spawnStage(1, teamId);
		}
			
		//spawn white flags for not existing teams
		if (!teams.containsKey(PanesterraTeamId.GAB1_SUB_DEST_69)) {
			SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(400030000, 804106, 282.73f, 289.1f, 687.38f, (byte) 1);
			SpawnEngine.spawnObject(template, 1);
		}
		if (!teams.containsKey(PanesterraTeamId.GAB1_SUB_DEST_70)) {
			SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(400030000, 804108, 282.49f, 739.62f, 689.66f, (byte) 1);
			SpawnEngine.spawnObject(template, 1);
		}
		if (!teams.containsKey(PanesterraTeamId.GAB1_SUB_DEST_71)) {
			SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(400030000, 804110, 734.06f, 740.75f, 681.16f, (byte) 1);
			SpawnEngine.spawnObject(template, 1);
		}
		if (!teams.containsKey(PanesterraTeamId.GAB1_SUB_DEST_72)) {
			SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(400030000, 804112, 738.58f, 286.02f, 680.71f, (byte) 1);
			SpawnEngine.spawnObject(template, 1);
		}
	}
	
	private void teleportPlayers() {
		for (PanesterraTeam team : teams.values()) {
			if (teams != null && !team.getTeamMembers().isEmpty()) {
				team.teleportToStartPosition();
			}
		}
	}
	
	private void instanceStartTimer() {
		Map<Integer, Integer> messages = new FastMap<>();
		messages.put(10000, 1402252); //starts soon
		messages.put(40000, 1402252); //starts soon
		messages.put(60000, 1402255); //4min till start
		messages.put(180000, 1402256); //2min till start
		messages.put(240000, 1402257); //1min till start
		messages.put(270000, 1402586); //30s till start
		
		for (Entry<Integer, Integer> e : messages.entrySet()) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					if (started && status == AhserionInstanceStatus.PREPARING_INSTANCE_START)
						sendMessage(0, e.getValue());
				}
			}, e.getKey());
		}
		
		//spawn mobs 30s before doors are opened
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (started && status == AhserionInstanceStatus.PREPARING_INSTANCE_START)
					spawnStage(2, PanesterraTeamId.BALAUR);
			}
		}, 270000);
		
		//Open Doors after 5min
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (!started || status != AhserionInstanceStatus.PREPARING_INSTANCE_START) {
					return;
				}
				synchronized (this) {
					status = AhserionInstanceStatus.INSTANCE_RUNNING;
				}
				for (StaticDoor door : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().getDoors().values()) {
					door.setOpen(true);
				}
				sendMessage(0, 1402587);
				scheduleCorridorShieldSpawn();
				scheduleAttack(1); //12min
				scheduleAttack(2); //29min
				startInstanceChecker();
			}
		}, 301000); //5min 1sec
		
		//stop instance 60 minutes after start
		instanceTimer = ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
			onStop();
			}
		}, 3601000); // 60min 1sec (-5min preparation time -15min barricade invulnerable time = 40min effective time to kill ahserion)
	}
	
	private void scheduleCorridorShieldSpawn() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (started && status == AhserionInstanceStatus.INSTANCE_RUNNING) {
					sendMessage(0, 1402637);
					for (PanesterraTeamId teamId : teams.keySet()) {
						if (teamId != null) {
							spawnStage(3, teamId);
						}
					}
				}
			}
		}, 300000); //5min
	}
	
	private void scheduleAttack(int x) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (started && status == AhserionInstanceStatus.INSTANCE_RUNNING) {
					for (PanesterraTeamId teamId: teams.keySet()) {
						if (teamId != null && !teams.get(teamId).isEliminated()) {
							spawnStage(4, teamId);
						}
					}
				}
			}
		}, x * 1020000); //x * 12min after corridor shield spawned
	}
	
	private void startInstanceChecker() {
		instanceChecker = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				Iterator<Player> iter = World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().playerIterator();
				while (iter.hasNext()) {
					Player player = iter.next();
					if (player != null && !player.isGM()) {
						if (player.getPanesterraTeam() == null || player.getPanesterraTeam().isEliminated()) {
							player.setPanesterraTeam(null);
							leaveTeam(player);
							TeleportService2.moveToBindLocation(player, true);
						}
					}
				}
			}
		}, 5000, 60000);
	}
	
	public void onStop() {
		synchronized (this) {
				started = false;
				status = AhserionInstanceStatus.OFF;
		}

		PanesterraMatchmakingService.getInstance().onStop();
		winner = null;
		if (instanceChecker != null && !instanceChecker.isCancelled()) {
			instanceChecker.cancel(true);
		}
		if (registrationTimer != null && !registrationTimer.isCancelled()) {
			registrationTimer.cancel(true);
		}
		for (PanesterraTeam team : teams.values()) {
			team.setIsEliminated(true);
			team.moveToBindPoint();
		}
		teams.clear();
		despawnAll();
		for (StaticDoor door : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().getDoors().values()) {
			door.setOpen(false);
		}
	}
	
	public void spawnStage(int stage, PanesterraTeamId team) {
		if (team != PanesterraTeamId.BALAUR && stage >= 180 && stage <= 183) {
			if (teams.get(team) == null || teams.get(team).getTeamMembers().isEmpty()) {
				return;
			}
		}
		List<SpawnGroup2> ahserionSpawns = DataManager.SPAWNS_DATA2.getAhserionSpawnByTeamId(team.getId());
		if (ahserionSpawns == null) {
			return;
		}
		
		for (SpawnGroup2 grp : ahserionSpawns) {
			for (SpawnTemplate template : grp.getSpawnTemplates()) {
				AhserionsFlightSpawnTemplate ahserionTemplate = (AhserionsFlightSpawnTemplate) template;
				if (ahserionTemplate.getStage() == stage) {
					SpawnEngine.spawnObject(ahserionTemplate, 1);
				}
			}
		}
	}

	public boolean revivePlayer(Player player, int skillId) {
		if (!started || status == AhserionInstanceStatus.OFF ||
			player.getPanesterraTeam() == null || player.getPanesterraTeam().isEliminated()) {
			return false;
		}
		PlayerReviveService.revive(player, 25, 25, false, skillId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
		PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
		WorldPosition pos = player.getPanesterraTeam().getStartPosition();
		if (pos != null) {
			TeleportService2.teleportTo(player, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ());
		} else {
			TeleportService2.moveToBindLocation(player, true);
		}
		player.unsetResPosState();
		return true;
	}

	/**
	 * @param owner
	 * @param winner
	 */
	public void bossKilled(Npc owner, PanesterraTeamId winnerTeam) {
		if (winnerTeam == null || winnerTeam == PanesterraTeamId.BALAUR || !teams.containsKey(winnerTeam) || (teams.containsKey(winnerTeam) && teams.get(winnerTeam).isEliminated())) {
			synchronized (this) {
				status = AhserionInstanceStatus.OFF;
			}
			//something went wrong, remove all players from the map
			owner.getController().onDelete();
			for (PanesterraTeam team : teams.values()) {
				if (team != null) {
					team.setIsEliminated(true);
					team.moveToBindPoint();
				}
			}
			Iterator<VisibleObject> iter = World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().objectIterator();
			while (iter.hasNext()) {
				VisibleObject obj = iter.next();
				if (obj instanceof Npc) {
					((Npc) obj).getController().onDelete();
				} else if (obj instanceof Player) {
					Player player = (Player) obj;
					if (player != null && !player.isGM()) {
						player.setPanesterraTeam(null);
						leaveTeam(player);
						TeleportService2.moveToBindLocation(player, true);
					}
				}
			}
			onStop();
			return;
		}
		if (instanceTimer != null) {
			if (instanceTimer.isDone() || instanceTimer.isCancelled()) {
				return;
			} 
			if (!instanceTimer.cancel(false)) {
				return;
			}
		} else {
			return;	
		}
		synchronized (this) {
				status = AhserionInstanceStatus.INSTANCE_FINISHED;
				this.winner = (AhserionTeam) teams.get(winnerTeam);
		}
		for (PanesterraTeamId teamId : teams.keySet()) {
			if (teamId != null && teamId != winnerTeam) {
				teams.get(teamId).setIsEliminated(true);
				teams.get(teamId).moveToBindPoint();
			}
		}
		for (Npc npc : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().getNpcs()) {
			if (npc != null && npc.getNpcId() != owner.getNpcId()) {
				npc.getController().onDelete();
			}
		}
		scheduleStop();
	}
	
	public void corridorShieldDestroyed(int npcId) {
		if (!started || status != AhserionInstanceStatus.INSTANCE_RUNNING) {
			return;
		}
		if (npcId == 297306) {
			PanesterraTeam eliminatedTeam = teams.get(PanesterraTeamId.GAB1_SUB_DEST_69);
			if (eliminatedTeam != null) {
				synchronized (this) {
					teams.remove(PanesterraTeamId.GAB1_SUB_DEST_69);
				}
				eliminatedTeam.setIsEliminated(true);
				eliminatedTeam.moveToBindPoint();
				deleteNpcsByTeamId(PanesterraTeamId.GAB1_SUB_DEST_69);
				//spawn white flag
				SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(400030000, 804106, 282.73f, 289.1f, 687.38f, (byte) 1);
				SpawnEngine.spawnObject(template, 1);
			}
		} else if (npcId == 297307) {
			PanesterraTeam eliminatedTeam = teams.get(PanesterraTeamId.GAB1_SUB_DEST_70);
			if (eliminatedTeam != null) {
				synchronized (this) {
					teams.remove(PanesterraTeamId.GAB1_SUB_DEST_70);
				}
				eliminatedTeam.setIsEliminated(true);
				eliminatedTeam.moveToBindPoint();
				deleteNpcsByTeamId(PanesterraTeamId.GAB1_SUB_DEST_70);
				SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(400030000, 804108, 282.49f, 739.62f, 689.66f, (byte) 1);
				SpawnEngine.spawnObject(template, 1);
			}
		} else if (npcId == 297308) {
			PanesterraTeam eliminatedTeam = teams.get(PanesterraTeamId.GAB1_SUB_DEST_71);
			if (eliminatedTeam != null) {
				synchronized (this) {
					teams.remove(PanesterraTeamId.GAB1_SUB_DEST_71);
				}
				eliminatedTeam.setIsEliminated(true);
				eliminatedTeam.moveToBindPoint();
				deleteNpcsByTeamId(PanesterraTeamId.GAB1_SUB_DEST_71);
				SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(400030000, 804110, 734.06f, 740.75f, 681.16f, (byte) 1);
				SpawnEngine.spawnObject(template, 1);
			}
		} else if (npcId == 297309) {
			PanesterraTeam eliminatedTeam = teams.get(PanesterraTeamId.GAB1_SUB_DEST_72);
			if (eliminatedTeam != null) {
				synchronized (this) {
					teams.remove(PanesterraTeamId.GAB1_SUB_DEST_72);
				}
				eliminatedTeam.setIsEliminated(true);
				eliminatedTeam.moveToBindPoint();
				deleteNpcsByTeamId(PanesterraTeamId.GAB1_SUB_DEST_72);
				SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(400030000, 804112, 738.58f, 286.02f, 680.71f, (byte) 1);
				SpawnEngine.spawnObject(template, 1);
			}
		}
	}
	
	public void despawnAll() {
		for (Npc npc : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().getNpcs()) {
			if (npc != null)
				npc.getController().onDelete();
		}
	}
	
	private void deleteNpcsByTeamId(PanesterraTeamId id) {
		for (Npc npc : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().getNpcs()) {
			if (npc.getSpawn().getStaticId() >= 180 && npc.getSpawn().getStaticId() <= 183 || npc.isFlag()) {
				continue;
			}
			if (npc!= null && !npc.getLifeStats().isAlreadyDead() && npc.getSpawn() instanceof AhserionsFlightSpawnTemplate) {
				AhserionsFlightSpawnTemplate template = (AhserionsFlightSpawnTemplate) npc.getSpawn();
				if (template.getTeam() == id) {
					npc.getController().onDelete();
				}
			}
		}
	}

	public void onPlayerLogin(Player player) {
		if (started){
			if (status == AhserionInstanceStatus.PREPARING_REGISTRATION) {
				if (PanesterraMatchmakingService.getInstance().isPlayerRegistered(player)) {
					PacketSendUtility.sendWhiteMessageOnCenter(player, "You are currently registered for Ahserions Flight.");
				}
			} else if (status == AhserionInstanceStatus.PREPARING_INSTANCE_START || status == AhserionInstanceStatus.INSTANCE_RUNNING) {
				player.setPanesterraTeam(getPlayersTeam(player));
			} else if (status == AhserionInstanceStatus.INSTANCE_FINISHED) {
				if (winner != null) {
					if (winner.getTeamMembers() != null && winner.getTeamMembers().contains(player.getObjectId())) {
					player.setPanesterraTeam(winner);
					}
				}
			}
		}
		validatePlayer(player);
	}
	
	private void validatePlayer(Player player) {
		if (player.isGM()) {
			return;
		}
		if ((player.getPanesterraTeam() == null || (player.getPanesterraTeam() != null && player.getPanesterraTeam().isEliminated())) && player.getPosition().getMapId() == 400030000) {
			TeleportService2.moveToBindLocation(player, false);
		} else if (player.getPanesterraTeam() != null && !player.getPanesterraTeam().isEliminated() && player.getPosition().getMapId() != 400030000) {
			WorldPosition pos = player.getPanesterraTeam().getStartPosition();
			if (pos != null)
				World.getInstance().setPosition(player, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
		}
	}
	
	public PanesterraTeam getPlayersTeam(Player player) {
		if (teams == null || teams.isEmpty() || player == null) {
			return null;
		}
		for (int i=0; i < 4; i++) {
			PanesterraTeam tempTeam = null;
			switch (i) {
				case 0:
					tempTeam = teams.get(PanesterraTeamId.GAB1_SUB_DEST_69);
					break;
				case 1:
					tempTeam = teams.get(PanesterraTeamId.GAB1_SUB_DEST_70);
					break;
				case 2:
					tempTeam = teams.get(PanesterraTeamId.GAB1_SUB_DEST_71);
					break;
				case 3:
					tempTeam = teams.get(PanesterraTeamId.GAB1_SUB_DEST_72);
					break;
			}
			if (tempTeam != null && !tempTeam.isEliminated()) {
				if (tempTeam.getTeamMembers() != null && tempTeam.getTeamMembers().contains(player.getObjectId())) {
					return tempTeam;
				} 
			}
		} 
		return null;
	}
	
	private void sendMessage(int teamId, int msgId) {
		World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				if (player != null) {
					if (teamId == 0) {
						PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msgId));
					} else if (player.getPanesterraTeam() != null && player.getPanesterraTeam().getTeamId().getId() == teamId) {
						PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msgId));
					}
				}
			}
		});
	}
	
	private void leaveTeam(Player player) {
		if (player.isInAlliance2()) {
			PlayerAllianceService.removePlayer(player);
		} else if (player.isInGroup2()) {
			PlayerGroupService.removePlayer(player);
		}
	}
	
	private void scheduleStop() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				onStop();
			}
		}, 900000); //15min
	}
	
	public boolean isTeamNotEliminated(PanesterraTeamId team) {
		return (teams.get(team) != null && !teams.get(team).isEliminated());
	}
	
	public AhserionInstanceStatus getStatus() {
		return status;
	}
	
	public boolean isStarted() {
		return started;
	}
}
