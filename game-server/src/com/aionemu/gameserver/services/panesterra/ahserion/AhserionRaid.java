package com.aionemu.gameserver.services.panesterra.ahserion;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.BindPointPosition;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Yeats
 * @modified Estrayl, Neon
 */
public class AhserionRaid {

	private AtomicReference<AhserionRaidStatus> status = new AtomicReference<>(AhserionRaidStatus.OFF);
	private Map<PanesterraTeamId, PanesterraTeam> teams = null;
	private AhserionTeam winner = null;
	private Future<?> observeTask, progressTask;

	public static AhserionRaid getInstance() {
		return SingletonHolder.instance;
	}

	public boolean start() {
		if (!status.compareAndSet(AhserionRaidStatus.OFF, AhserionRaidStatus.PREPARING_REGISTRATION))
			return false;
		PanesterraMatchmakingService.getInstance().onStart();
		ThreadPoolManager.getInstance().schedule(() -> startInstancePreparation(), 600 * 1000);
		return true;
	}

	private void startInstancePreparation() {
		if (!status.compareAndSet(AhserionRaidStatus.PREPARING_REGISTRATION, AhserionRaidStatus.PREPARING_INSTANCE_START))
			return;
		PanesterraMatchmakingService.getInstance().prepareTeams();
		teams = PanesterraMatchmakingService.getInstance().getRegisteredTeams();
		if (teams != null && !teams.isEmpty()) {
			PanesterraMatchmakingService.getInstance().onStop();
			// move players to the instance and start instance timer
			spawnInstance();
			teleportPlayers();
			instanceStartTimer();
		} else {
			stop();
		}
	}

	private void spawnInstance() {
		// spawn Barricades & Tank Fleets
		spawnStage(0, PanesterraTeamId.BALAUR);
		spawnStage(180, PanesterraTeamId.BALAUR);
		spawnStage(181, PanesterraTeamId.BALAUR);
		spawnStage(182, PanesterraTeamId.BALAUR);
		spawnStage(183, PanesterraTeamId.BALAUR);

		// spawn flags & cannons for all registered teams
		for (PanesterraTeamId teamId : teams.keySet()) {
			if (teamId != null)
				spawnStage(0, teamId);
			spawnStage(1, teamId);
		}

		// spawn white flags for not existing teams
		if (!teams.containsKey(PanesterraTeamId.GAB1_SUB_DEST_69)) {
			SpawnTemplate template = SpawnEngine.newSingleTimeSpawn(400030000, 804106, 282.73f, 289.1f, 687.38f, (byte) 1);
			SpawnEngine.spawnObject(template, 1);
		}
		if (!teams.containsKey(PanesterraTeamId.GAB1_SUB_DEST_70)) {
			SpawnTemplate template = SpawnEngine.newSingleTimeSpawn(400030000, 804108, 282.49f, 739.62f, 689.66f, (byte) 1);
			SpawnEngine.spawnObject(template, 1);
		}
		if (!teams.containsKey(PanesterraTeamId.GAB1_SUB_DEST_71)) {
			SpawnTemplate template = SpawnEngine.newSingleTimeSpawn(400030000, 804110, 734.06f, 740.75f, 681.16f, (byte) 1);
			SpawnEngine.spawnObject(template, 1);
		}
		if (!teams.containsKey(PanesterraTeamId.GAB1_SUB_DEST_72)) {
			SpawnTemplate template = SpawnEngine.newSingleTimeSpawn(400030000, 804112, 738.58f, 286.02f, 680.71f, (byte) 1);
			SpawnEngine.spawnObject(template, 1);
		}
	}

	private void teleportPlayers() {
		for (PanesterraTeam team : teams.values())
			if (!team.getMembers().isEmpty())
				team.teleportToStartPosition();
	}

	private void instanceStartTimer() {
		progressTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			int progress;

			@Override
			public void run() {
				switch (progress++) {
					case 1:
						sendMessage(0, SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_ALARM_01());
						break;
					case 6:
						sendMessage(0, SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_ALARM_04());
						break;
					case 18:
						sendMessage(0, SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_ALARM_05());
						break;
					case 24:
						sendMessage(0, SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_ALARM_06());
						break;
					case 27:
						sendMessage(0, SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_ALARM_07());
						spawnStage(2, PanesterraTeamId.BALAUR); // spawn mobs 30s before doors are opened
						break;
					case 30:
						status.set(AhserionRaidStatus.INSTANCE_RUNNING);
						for (StaticDoor door : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().getDoors().values())
							door.setOpen(true);
						sendMessage(0, SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_ALARM_08());
						scheduleObservation();
						break;
					case 60:
						sendMessage(0, SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_ALARM_09());
						for (PanesterraTeamId teamId : teams.keySet())
							spawnStage(3, teamId);
						break;
					case 102:
					case 204:
						for (PanesterraTeamId teamId : teams.keySet())
							if (teamId != null && !teams.get(teamId).isEliminated())
								spawnStage(4, teamId);
						break;
					case 360:
						stop(); // stop after 60min (-5min preparation time -15min barricade invulnerable time = 40min effective time to kill Ahserion)
						break;
				}
			}
		}, 10000, 10000);
	}

	private void scheduleObservation() {
		observeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			for (Player player : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().getPlayersInside()) {
				if (!player.isStaff()) {
					if (player.getPanesterraTeam() == null || player.getPanesterraTeam().isEliminated()) {
						player.setPanesterraTeam(null);
						leaveTeam(player);
						TeleportService.moveToBindLocation(player);
					}
				}
			}
		}, 5000, 60000);
	}

	public boolean stop() {
		if (status.getAndSet(AhserionRaidStatus.OFF) == AhserionRaidStatus.OFF)
			return false;
		PanesterraMatchmakingService.getInstance().onStop();
		winner = null;
		cancelTask(observeTask);
		cancelTask(progressTask);
		if (teams != null) {
			for (PanesterraTeam team : teams.values()) {
				team.setIsEliminated(true);
				team.moveToBindPoint();
			}
			teams.clear();
		}
		for (VisibleObject obj : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance()) {
			if (obj instanceof Player) { // in case someone wasn't ported via team.moveToBindPoint
				Player player = (Player) obj;
				if (!player.isStaff()) {
					player.setPanesterraTeam(null);
					leaveTeam(player);
					TeleportService.moveToBindLocation(player);
				}
			} else if (obj instanceof StaticDoor) {
				((StaticDoor) obj).setOpen(false);
			} else if (obj instanceof Npc) {
				obj.getController().delete();
			}
		}
		return true;
	}

	public void spawnStage(int stage, PanesterraTeamId team) {
		if (team != PanesterraTeamId.BALAUR && stage >= 180 && stage <= 183)
			if (teams.get(team) == null || teams.get(team).getMembers().isEmpty())
				return;

		List<SpawnGroup> ahserionSpawns = DataManager.SPAWNS_DATA.getAhserionSpawnByTeamId(team.getId());
		if (ahserionSpawns == null)
			return;

		for (SpawnGroup grp : ahserionSpawns) {
			for (SpawnTemplate template : grp.getSpawnTemplates()) {
				AhserionsFlightSpawnTemplate ahserionTemplate = (AhserionsFlightSpawnTemplate) template;
				if (ahserionTemplate.getStage() == stage)
					SpawnEngine.spawnObject(ahserionTemplate, 1);
			}
		}
	}

	public boolean revivePlayer(Player player, int skillId) {
		if (status.get() == AhserionRaidStatus.OFF || player.getPanesterraTeam() == null || player.getPanesterraTeam().isEliminated())
			return false;

		PlayerReviveService.revive(player, 25, 25, false, skillId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
		PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
		WorldPosition pos = player.getPanesterraTeam().getStartPosition();
		if (pos != null)
			TeleportService.teleportTo(player, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ());
		else
			TeleportService.moveToBindLocation(player);
		player.unsetResPosState();
		return true;
	}

	public void bossKilled(Npc owner, PanesterraTeamId winnerTeam) {
		if (!teams.containsKey(winnerTeam) || teams.get(winnerTeam).isEliminated()) {
			// something went wrong, remove all players from the map
			LoggerFactory.getLogger(AhserionRaid.class).warn("Ahserion got killed but winnerTeam is missing or eliminated. No rewards for players.");
			stop();
			return;
		}
		cancelTask(progressTask);
		status.set(AhserionRaidStatus.INSTANCE_FINISHED);
		winner = (AhserionTeam) teams.get(winnerTeam);
		for (PanesterraTeamId teamId : teams.keySet()) {
			if (teamId != null && teamId != winnerTeam) {
				teams.get(teamId).setIsEliminated(true);
				teams.get(teamId).moveToBindPoint();
			}
		}
		for (Npc npc : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().getNpcs()) {
			if (npc.getNpcId() != owner.getNpcId())
				npc.getController().delete();
		}
		scheduleStop();
	}

	public void corridorShieldDestroyed(int npcId) {
		if (status.get() != AhserionRaidStatus.INSTANCE_RUNNING)
			return;

		PanesterraTeamId eliminated = null;
		SpawnTemplate template = null;

		switch (npcId) {
			case 297306:
				eliminated = PanesterraTeamId.GAB1_SUB_DEST_69;
				template = SpawnEngine.newSingleTimeSpawn(400030000, 804106, 282.73f, 289.1f, 687.38f, (byte) 1);
				break;
			case 297307:
				eliminated = PanesterraTeamId.GAB1_SUB_DEST_70;
				template = SpawnEngine.newSingleTimeSpawn(400030000, 804108, 282.49f, 739.62f, 689.66f, (byte) 1);
				break;
			case 297308:
				eliminated = PanesterraTeamId.GAB1_SUB_DEST_71;
				template = SpawnEngine.newSingleTimeSpawn(400030000, 804110, 734.06f, 740.75f, 681.16f, (byte) 1);
				break;
			case 297309:
				eliminated = PanesterraTeamId.GAB1_SUB_DEST_72;
				template = SpawnEngine.newSingleTimeSpawn(400030000, 804112, 738.58f, 286.02f, 680.71f, (byte) 1);
				break;
		}
		PanesterraTeam eliminatedTeam = null;
		synchronized (this) {
			eliminatedTeam = teams.remove(eliminated);
		}
		if (eliminatedTeam == null)
			return;
		eliminatedTeam.setIsEliminated(true);
		eliminatedTeam.moveToBindPoint();
		deleteNpcsByTeamId(eliminated);
		SpawnEngine.spawnObject(template, 1);
	}

	private void deleteNpcsByTeamId(PanesterraTeamId id) {
		for (Npc npc : World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().getNpcs()) {
			if (npc.getSpawn().getStaticId() >= 180 && npc.getSpawn().getStaticId() <= 183 || npc.isFlag())
				continue;
			if (!npc.isDead() && npc.getSpawn() instanceof AhserionsFlightSpawnTemplate) {
				AhserionsFlightSpawnTemplate template = (AhserionsFlightSpawnTemplate) npc.getSpawn();
				if (template.getTeam() == id)
					npc.getController().delete();
			}
		}
	}

	public void onPlayerLogin(Player player) {
		switch (status.get()) {
			case PREPARING_REGISTRATION:
				if (PanesterraMatchmakingService.getInstance().isPlayerRegistered(player))
					PacketSendUtility.sendMessage(player, "You are currently registered for Ahserions Flight.", ChatType.WHITE_CENTER);
				break;
			case PREPARING_INSTANCE_START:
			case INSTANCE_RUNNING:
				player.setPanesterraTeam(getPlayersTeam(player));
				break;
			case INSTANCE_FINISHED:
				if (winner != null) {
					if (winner.getMembers() != null && winner.getMembers().contains(player.getObjectId()))
						player.setPanesterraTeam(winner);
				}
				break;
		}
		validatePlayer(player);
	}

	private void validatePlayer(Player player) {
		if (player.isStaff())
			return;
		if ((player.getPanesterraTeam() == null || (player.getPanesterraTeam() != null && player.getPanesterraTeam().isEliminated()))
			&& player.getPosition().getMapId() == 400030000) {
			BindPointPosition bp = player.getBindPoint();
			World.getInstance().setPosition(player, bp.getMapId(), bp.getX(), bp.getY(), bp.getZ(), bp.getHeading());
		} else if (player.getPanesterraTeam() != null && !player.getPanesterraTeam().isEliminated() && player.getPosition().getMapId() != 400030000) {
			WorldPosition pos = player.getPanesterraTeam().getStartPosition();
			if (pos != null)
				World.getInstance().setPosition(player, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
		}
	}

	public PanesterraTeam getPlayersTeam(Player player) {
		if (teams == null || teams.isEmpty() || player == null)
			return null;

		for (int i = 0; i < 4; i++) {
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
			if (tempTeam != null && !tempTeam.isEliminated())
				if (tempTeam.getMembers() != null && tempTeam.getMembers().contains(player.getObjectId()))
					return tempTeam;
		}
		return null;
	}

	private void sendMessage(int teamId, SM_SYSTEM_MESSAGE msg) {
		World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player player) {
				if (teamId == 0)
					PacketSendUtility.sendPacket(player, msg);
				else if (player.getPanesterraTeam() != null && player.getPanesterraTeam().getTeamId().getId() == teamId)
					PacketSendUtility.sendPacket(player, msg);
			}
		});
	}

	private void leaveTeam(Player player) {
		if (player.isInAlliance())
			PlayerAllianceService.removePlayer(player);
		else if (player.isInGroup())
			PlayerGroupService.removePlayer(player);
	}

	private void scheduleStop() {
		ThreadPoolManager.getInstance().schedule(() -> stop(), 900000); // 15min
	}

	private void cancelTask(Future<?> task) {
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}

	public boolean isTeamNotEliminated(PanesterraTeamId team) {
		return teams.get(team) != null && !teams.get(team).isEliminated();
	}

	public AhserionRaidStatus getStatus() {
		return status.get();
	}

	public boolean isStarted() {
		return status.get() != AhserionRaidStatus.OFF;
	}

	private static class SingletonHolder {

		protected static final AhserionRaid instance = new AhserionRaid();
	}
}
