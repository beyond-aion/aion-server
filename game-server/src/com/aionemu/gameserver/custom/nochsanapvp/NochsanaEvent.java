package com.aionemu.gameserver.custom.nochsanapvp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;

import com.aionemu.gameserver.custom.BattleService;
import com.aionemu.gameserver.custom.GameEvent;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Woge
 */

public class NochsanaEvent extends GameEvent {

	private List<CustomBase> bases = new LinkedList<>();
	private CustomBase customBase = null;
	private List<CustomBase> defenderBases = new LinkedList<>();
	private List<CustomBase> attackerBases = new LinkedList<>();
	private Map<Integer, VisibleObject> spawnControl = new HashMap<>();
	private Map<Npc, CustomBase> teleportToBase = new HashMap<>();
	private Future<?> timeout;
	private long definedEnd;

	public NochsanaEvent() {
		super();
		InstanceService.getNextAvailableInstance(300030000, 0, (byte) 0, this);
	}

	protected void assignTeleporterToBase(Npc npc, CustomBase base) {
		Iterator<CustomBase> iter = teleportToBase.values().iterator();
		while (iter.hasNext()) {
			CustomBase b = iter.next();
			if (b == base) {
				iter.remove();
			}
		}
		teleportToBase.put(npc, base);
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		CustomBase base = teleportToBase.get(npc);
		if (player.getRace() != base.getOwner()) {
			BattleService.sendBattleNotice(player, "This Portal is owned by another faction!");
			return;
		}

		Npc target = base.getTarget();
		TeleportService.teleportTo(player, this.mapId, this.instanceId, target.getX(), target.getY(), target.getZ(), target.getHeading(),
			TeleportAnimation.JUMP_IN_STATUE);

	}

	@Override
	public void onInstanceCreate(WorldMapInstance in) {
		super.onInstanceCreate(in);
		// Wall Spawns
		spawn(700998, 348.20752f, 283.81482f, 384.2954f, new Byte("72"));
		spawn(700998, 319.3013f, 287.37277f, 384.2954f, new Byte("105"));
		spawnControl.put(0, spawn(700998, 517.8498f, 671.2072f, 331.0f, new Byte("11")));
		spawnControl.put(1, spawn(700998, 334.49713f, 292.2329f, 384.2954f, new Byte("87")));
	}

	private synchronized void sortBase(CustomBase base, Race race) {
		if (race == defender) {
			attackerBases.remove(base);
			defenderBases.add(base);
		} else {
			defenderBases.remove(base);
			attackerBases.add(base);
		}

		attackerBases.sort(null);
		defenderBases.sort(null);
	}

	@Override
	public boolean onReviveEvent(Player player) {
		applyBuffs(player);
		return super.onReviveEvent(player);
	}

	private void applyBuffs(Player player) {
		if (checkBuffCondition(player)) {
			for (CustomBase base : bases) {
				if (base.getOwner() == player.getRace()) {
					SkillEngine.getInstance().applyEffectDirectly(base.getBuffId(), player, player, 0);
				}
			}
		}
	}

	private boolean checkBuffCondition(Player player) {
		if (player.getRace() != this.defender) {
			return true;
		}

		int diff = this.getPlayerByRace(this.defender == Race.ELYOS ? Race.ASMODIANS : Race.ELYOS).size() - this.getPlayerByRace(this.defender).size();

		if (diff >= 2) {
			return true;
		}

		return false;
	}

	@Override
	protected void handleGameStart() {
		bases.add(new CustomBase(10, this, false, defender, 12115));
		bases.add(new CustomBase(11, this, false, defender, 12116));
		bases.add(new CustomBase(12, this, false, defender, 0));
		customBase = new CustomBase(13, this, true, defender, 0);
		defenderBases.addAll(bases);
		defenderBases.sort(null);
		// Spawn Teleporter
		// CustomBase base1 = new CustomBase(21, this, false, defender, 0);
		// CustomBase base2 = new CustomBase(22, this, false, defender, 0);

		for (Player player : participants) {
			this.applyBuffs(player);
		}

		for (VisibleObject o : spawnControl.values()) {
			o.getController().delete();
		}

		this.sendMajorTimer(20 * 60);
		this.definedEnd = System.currentTimeMillis() + 20 * 60 * 1000;
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				onEventTimeout();
			}
		}, 20 * 60 * 1000);

		announceAll("Nochsana is under Siege! Fight!");

	}

	private void sendRemainingMajorTimer() {
		int time = (int) ((this.definedEnd - System.currentTimeMillis()) / 1000);
		this.sendMajorTimer(time);
	}

	private void onEventTimeout() {
		this.announceAll("The Battle is Over");
		if (attackerBases.size() >= bases.size() / 2) {
			this.announceAll("Attackers are Controlling the half of all Bases. Attackers win.");
			onEventEnd(this.defender == Race.ELYOS ? Race.ASMODIANS : Race.ELYOS);
		} else {
			this.announceAll("Defenders are Controlling more than the half of all Bases. Defenders win.");
			onEventEnd(this.defender);
		}
	}

	protected void checkWorldCapture(CustomBase base, Race winner) {
		if (base.isTeleport()) {
			return;
		}

		if (base == customBase) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					onEventEnd(winner);
				}
			}, 1 * 1000);
			return;
		}

		sortBase(base, winner);

		for (Player player : this.getPlayerByRace(this.defender == Race.ELYOS ? Race.ASMODIANS : Race.ELYOS)) {
			if (winner != this.defender) {
				SkillEngine.getInstance().applyEffectDirectly(base.getBuffId(), player, player, 0);
			} else {
				player.getEffectController().removeEffect(base.getBuffId());
			}
		}

		if (winner == defender && customBase.getBoss() != null || winner == defender && customBase.isPreparingToSpawn()) {
			sendCornerTimer(0);
			this.sendRemainingMajorTimer();
			stopSiege();
		}

		if (winner != defender && timeout != null && !timeout.isCancelled()) {
			timeout.cancel(false);
			this.sendCornerTimer(0);
			this.sendRemainingMajorTimer();
			this.announceAll("Defenders lost a Base");
		}

		if (checkAllCaptures(winner)) {
			if (winner == defender) {
				sendCornerTimer(60);
				announceAll("Defenders captured all Bases. They will win in 60 seconds.");

				timeout = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						onEventEnd(defender);
					}
				}, 60 * 1000);
				return;
			}

			customBase.scheduleBossRespawn();
			announceAll(winner.name() + " captured all bases. The fortress boss will arrive in less than 30 seconds");
			sendCornerTimer(30);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					sendRemainingMajorTimer();
				}
			}, 31 * 1000);
			return;
		}

	}

	private boolean checkAllCaptures(Race winner) {
		for (CustomBase base : bases) {
			if (base.getOwner() != winner) {
				return false;
			}
		}

		return true;
	}

	private void stopSiege() {
		customBase.deleteBoss();
		announceAll("The fortress is now immortal");
	}

	@Override
	protected synchronized Point3D getSpawn(Player player) {

		if (currentStage == EventStage.IDLE || currentStage == EventStage.WAITING) {
			return getInitialSpawn(player.getRace());
		}

		CustomBase respawnBase = null;

		try {
			if (player.getRace() == defender) {
				respawnBase = defenderBases.get(1);
			} else {
				respawnBase = attackerBases.get(attackerBases.size() - 2);
			}
		} catch (NoSuchElementException | IndexOutOfBoundsException ex) {
			respawnBase = null;
		}

		if (respawnBase == null) {
			return getInitialSpawn(player.getRace());
		}

		WorldPosition position = respawnBase.getFlag().getPosition();
		return new Point3D(position.getX(), position.getY(), position.getZ());
	}

	private Point3D getInitialSpawn(Race race) {
		if (race == defender) {
			return new Point3D(332.37943f, 275.0666f, 385.55338f);
		}
		return new Point3D(521.75507f, 672.61615f, 332.13452f);
	}
}
