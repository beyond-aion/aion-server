package com.aionemu.gameserver.custom.nochsanapvp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.concurrent.Future;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.GeneralAIEvent;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.custom.BattleService;
import com.aionemu.gameserver.custom.GameEvent;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Woge
 */

public class NochsanaEvent extends GameEvent {

	private List<CustomBase> bases = new LinkedList<CustomBase>();
	private CustomBase siegeBoss = null;
	private NavigableSet<CustomBase> defenderBases = new TreeSet<CustomBase>();
	private NavigableSet<CustomBase> attackerBases = new TreeSet<CustomBase>();
	private Map<Integer, VisibleObject> spawnControl = new HashMap<Integer, VisibleObject>();
	private Map<Npc, CustomBase> teleportToBase = new HashMap<Npc, CustomBase>();
	private Race defender = Race.ASMODIANS;
	private Future<?> timeout;

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
			player.getLifeStats().reduceHp((player.getLifeStats().getCurrentHp() / 100) * 50, npc);
			BattleService.sendBattleNotice(player, "This Portal is owned by another faction!");
			return;
		}

		Npc target = base.getTarget();
		TeleportService2.teleportTo(player, this.mapId, this.instanceId, target.getX(), target.getY(), target.getZ(), target.getHeading(),
			TeleportAnimation.FADE_OUT_BEAM);

	}

	@Override
	public void onInstanceCreate(WorldMapInstance in) {
		super.onInstanceCreate(in);
		defender = (Rnd.get(0, 1) == 0 ? Race.ELYOS : Race.ASMODIANS);
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
			return;
		}
		defenderBases.remove(base);
		attackerBases.add(base);
	}

	@SuppressWarnings("unused")
	protected void handleGameStart() {
		bases.add(new CustomBase(10, this, false, defender));
		bases.add(new CustomBase(11, this, false, defender));
		bases.add(new CustomBase(12, this, false, defender));
		siegeBoss = new CustomBase(13, this, true, defender);
		defenderBases.addAll(bases);

		// Spawn Teleporter
		CustomBase base1 = new CustomBase(21, this, false, defender);
		CustomBase base2 = new CustomBase(22, this, false, defender);
		for (VisibleObject o : spawnControl.values()) {
			o.getController().delete();
		}
		announceAll("Go! Fight!");
	}

	protected void checkWorldCapture(CustomBase base, Race winner) {
		if (base.isTeleport()) {
			return;
		}

		if (base == siegeBoss) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					onEventEnd(winner);
				}
			}, 1 * 1000);
		}

		sortBase(base, winner);

		if (winner == defender && siegeBoss.getBoss() != null || winner == defender && siegeBoss.isPreparingToSpawn()) {
			stopSiege();
		}

		if (winner != defender && timeout != null && !timeout.isCancelled()) {
			timeout.cancel(false);
			sendCornerTimer(0);
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

			siegeBoss.scheduleBossRespawn();
			announceAll(winner.name() + " captured all bases. The fortress boss will arrive in less than 30 seconds");
			sendCornerTimer(30);
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
		siegeBoss.deleteBoss();
		announceAll("The fortress is now immortal");
		sendCornerTimer(0);
	}

	@Override
	protected synchronized Point3D getSpawn(Player player) {

		if (currentStage == EventStage.IDLE || currentStage == EventStage.WAITING) {
			return getInitialSpawn(player.getRace());
		}

		CustomBase respawnBase = null;

		try {
			if (player.getRace() == defender) {
				respawnBase = defenderBases.higher(defenderBases.first());
			} else {
				respawnBase = attackerBases.lower(defenderBases.last());
			}
		} catch (NoSuchElementException ex) {
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

@SuppressWarnings("rawtypes")
class CBaseDeathListener extends OnDieEventListener {

	private final CustomBase base;

	public CBaseDeathListener(CustomBase base) {
		this.base = base;
	}

	@Override
	public void onBeforeEvent(GeneralAIEvent event) {
		super.onBeforeEvent(event);

		if (!event.isHandled()) {
			return;
		}

		AionObject winner = base.getBoss().getAggroList().getMostDamage();

		if (winner instanceof Creature) {
			Creature killer = (Creature) winner;

			if (killer.getRace().isPlayerRace()) {
				base.capture(killer.getRace(), killer.getName(), (Player) killer);
			}

		} else if (winner instanceof TemporaryPlayerTeam) {
			TemporaryPlayerTeam team = (TemporaryPlayerTeam) winner;
			if (team.getRace().isPlayerRace())
				base.capture(team.getRace(), team.getLeader().getName(), team);
		}
	}

}
