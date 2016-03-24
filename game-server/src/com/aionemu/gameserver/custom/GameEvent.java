package com.aionemu.gameserver.custom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STANCE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HTMLService;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Woge
 */

public abstract class GameEvent extends GeneralInstanceHandler implements Comparable<GameEvent> {

	protected List<Player> participants = new LinkedList<Player>();
	protected Map<Player, EventStatistic> statistics = new HashMap<Player, EventStatistic>();

	protected enum EventStage {
		IDLE,
		WAITING,
		STARTING,
		LATE,
		AFTER_END
	};

	private boolean firstBlood = false;
	protected GameEventType gameEventType = null;
	protected EventStage currentStage = EventStage.IDLE;
	protected long openTime = 0;
	protected Race defender;

	public GameEvent() {
		super();
	}

	@Override
	public void onInstanceDestroy() {
		this.gameEventType.unsetActiveEvent(this);
	}
	
	public synchronized void unregisterParticipant(Player player) {
	 participants.remove(player);
	 statistics.remove(player);
	}

	@Override
	public void onPlayerLogOut(Player player) {
		player.getEffectController().removeAllEffects();
		TeleportService2.teleportToCapital(player);
		unregisterParticipant(player);
		tryDestroyInstance();
	}

	private void tryDestroyInstance() {
		if (participants.size() == 0) {
			this.gameEventType.unsetActiveEvent(this);
		}
	}
	
	@Override
	public void onLeaveInstance(Player player) {
		player.getEffectController().removeAllEffects();
		PacketSendUtility.sendPacket(player, new SM_PLAYER_STANCE(player, 0));
		unregisterParticipant(player);
		if(currentStage != EventStage.AFTER_END) {
			PacketSendUtility.sendMessage(player, "You left during an Event.\n Please note"
				+ " that your Teammates Need you and we will keep track of your amount of leaves.");
		}		
		BattleService.getInstance().unregisterPlayer(player);
		tryDestroyInstance();
	}

	public void setGameType(GameEventType type) {
		gameEventType = type;
	}

	@Override
	public void onInstanceCreate(WorldMapInstance in) {
		super.onInstanceCreate(in);
	}

	@Override
	public void onEnterInstance(Player player) {
		player.getEffectController().removeAllEffects();
		if (currentStage == EventStage.WAITING) {
			PacketSendUtility.sendPacket(player, new SM_PLAYER_STANCE(player, 2));
			long time = (openTime - System.currentTimeMillis());
			PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, (int) (time / 1000)));
			BattleService.sendBattleNotice(player, "Welcome to the Nochsana Battlegrounds");
		} else {
			BattleService.sendBattleNotice(player, "You're late, Daeva! The Battle is already on.");
		}
		HTMLService.showHTML(player, HTMLCache.getInstance().getHTML("nochsanaGuide.xhtml"));
	}

	public synchronized void onEventEnd(Race winningRace) {
		currentStage = EventStage.AFTER_END;
		Race looser = (winningRace == Race.ELYOS ? Race.ASMODIANS : Race.ELYOS);

		this.announceAll("# V I C T O R Y #", "Beyond Aion", winningRace);
		this.announceAll("# D E F E A T #", "Beyond Aion", looser);

		String text = "Thank you for participating in the Tests for our Event features."
			+ " \n Even tought there is still a lot of work to do, we want to reward your help with some items. \n"
			+ " Keep going! \n The Beyond Aion Team";

		List<Player> players = new LinkedList<Player>();
		players.addAll(participants);

		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			TeleportService2.teleportTo(p, statistics.get(p).getOrigin());
			if (BattleService.getInstance().getRewardId() != 0) {
				SystemMailService.getInstance().sendMail("Beyond Aion", p.getName(), "Reward for Participating", text,
					BattleService.getInstance().getRewardId(), 1, 0, LetterType.NORMAL);
			}
		}
	}

	@Override
	public boolean onReviveEvent(Player player) {
		if(this.currentStage == EventStage.AFTER_END) {
			return false;
		}
		Point3D respawn = this.getSpawn(player);
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		TeleportService2.teleportTo(player, this.mapId, this.instanceId, respawn.getX(), respawn.getY(), respawn.getZ(), new Byte("55"));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());

		return true;
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		Player killer = null;
		if (lastAttacker instanceof Player) {
			killer = (Player) lastAttacker;
		} else {
			if(player.getAggroList().getMostPlayerDamage() != null) {
				killer = player.getAggroList().getMostPlayerDamage();
			}		
		}
		
		if(killer != null) {
			statistics.get(killer).addKill();
			announceAll(killer.getName() + " has slain " + player.getName(), statistics.get(killer).getStreakName());
			
			if (!this.firstBlood) {
				announceAll(killer.getName() + " of " + killer.getRace().name() + " has drawn FIRST BLOOD");
				this.firstBlood = true;
			}
		} else {
			announceAll(player.getName() + " was EXECUTED");
		}
	
		statistics.get(player).addDeath();
		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));
		return true;
	}

	public int getInstanceId() {
		return this.instanceId;
	}

	public synchronized void announceAll(String announce, String sender) {
		for (Player player : participants) {
			BattleService.sendBattleNotice(player, announce, sender);
		}
	}

	public synchronized void announceAll(String announce, String sender, Race race) {
		for (Player player : participants) {
			if (player.getRace() == race) {
				BattleService.sendBattleNotice(player, announce, sender);
			}

		}
	}

	public synchronized void announceAll(String announce) {
		for (Player player : participants) {
			BattleService.sendBattleNotice(player, announce);
		}
	}

	public synchronized void sendCornerTimer(int seconds) {
		for (Player participant : participants) {
			PacketSendUtility.sendPacket(participant, new SM_QUEST_ACTION(0, seconds));
		}
	}
	
	public synchronized void sendMajorTimer(int seconds) {
		for (Player participant : participants) {
			PacketSendUtility.sendPacket(participant, new SM_QUEST_ACTION(-1, seconds));
		}
	}

	public VisibleObject spawnObject(int npcId, float x, float y, float z, byte heading, int respawnTime) {
		return super.spawn(npcId, x, y, z, heading);
	}

	public synchronized boolean registerPlayer(Player player) {
		if (participants.contains(player)) {
			return false;
		}

		if (getPlayerByRace(player.getRace()).size() < gameEventType.getMaxPlayerPerRace()) {
			participants.add(player);

			if (currentStage == EventStage.IDLE) {
				checkStartConditions();
			} else {
				teleportPlayersIn(0);
			}
			return true;
		}

		return false;
	}

	private void checkStartConditions() {
		if (participants.size() >= gameEventType.getMaxPlayerPerRace() * 2)  {
		if(currentStage != EventStage.WAITING) {
			currentStage = EventStage.WAITING;
			scheduleGameStart();
		}
		announceAll("Your Event is going to start within the next few seconds. Get out of Fight and prepare for the Teleport.");
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					teleportPlayersIn(0);
				}
			}, 5 * 1000);
		}
	}

	private synchronized List<Player> getMissingPlayers() {
		List<Player> missing = new LinkedList<Player>();

		for (Player player : participants) {
			if (player.getPosition().getWorldMapInstance().getInstanceHandler() != this) {
				missing.add(player);
			}
		}

		return missing;
	}

	private synchronized void teleportPlayersIn(int trys) {
		List<Player> missing = getMissingPlayers();

		for (Player player : missing) {

			if (player.getController().isInCombat()) {
				BattleService.sendBattleNotice(player, "You cannot join the event while in fight. Get out there!");
				continue;
			}
			statistics.put(player, new EventStatistic(player.getPosition()));
			Point3D spawn = getSpawn(player);
			TeleportService2.teleportTo(player, this.mapId, this.instanceId, spawn.getX(), spawn.getY(), spawn.getZ() + 1);

		}

		if (missing.isEmpty()) {
			return;
		}

		if (trys > 3) {
			for (Player player : missing) {
				BattleService.sendBattleNotice(player, "You have been excluded from the event.");
			}
		}

		final int tryed = trys += 1;
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				teleportPlayersIn(tryed);
			}
		}, 10 * 1000);
	}

	private void scheduleGameStart() {
		openTime = System.currentTimeMillis() + (40 * 1000);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				startGame();
			}
		}, 40 * 1000);
		
		if(this.getPlayerByRace(Race.ELYOS).size() < this.getPlayerByRace(Race.ASMODIANS).size()) {
			this.defender = Race.ELYOS;
		} else {
			this.defender= Race.ASMODIANS;
		}

	}

	public void startGame() {
		this.currentStage = EventStage.STARTING;
		for(Player player : participants) {
			PacketSendUtility.sendPacket(player, new SM_PLAYER_STANCE(player, 0));
		}
		handleGameStart();
	}

	protected synchronized List<Player> getPlayerByRace(Race race) {
		List<Player> result = new LinkedList<Player>();
		for (Player p : participants) {
			if (p.getRace() == race) {
				result.add(p);
			}
		}
		return result;
	}

	public int getMatchmakingPriority(Race race) {
		int difference = participants.size() - getPlayerByRace(race).size();
		switch (currentStage) {
			case IDLE:
				return 0;
			case WAITING:
				return 1 + difference;
			case STARTING:
				return 2 + difference;
			case LATE:
				return 40 + difference;
			default:
				return 0;
		}
	}

	public String getEventName() {
		return gameEventType.name().replace("_", " ").toLowerCase();
	}

	public void onBaseCapture(Player player) {
		statistics.get(player).addCapture();
	}

	protected abstract void handleGameStart();

	protected abstract Point3D getSpawn(Player player);

	@Override
	public int compareTo(GameEvent event) {
		if (event.getMatchmakingPriority(gameEventType.getComparableRace()) < this.getMatchmakingPriority(gameEventType.getComparableRace())) {
			return -1;
		}
		if (event.getMatchmakingPriority(gameEventType.getComparableRace()) > this.getMatchmakingPriority(gameEventType.getComparableRace())) {
			return 1;
		}
		return 0;
	}

}
