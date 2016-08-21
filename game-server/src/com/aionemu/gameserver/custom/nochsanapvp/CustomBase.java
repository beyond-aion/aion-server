package com.aionemu.gameserver.custom.nochsanapvp;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.GeneralAIEvent;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.custom.BattleService;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Woge
 */

public class CustomBase implements Comparable<CustomBase> {

	private Npc boss, flag;
	private NochsanaEvent ownerEvent = null;
	private final CBaseDeathListener bossDeathListener;
	private Race owner;
	private int id;
	private Lock lock = new ReentrantLock();
	private Future<?> bossRespawn = null;
	private boolean siegeBoss = false;
	private boolean isTeleport = false;
	private Npc target;
	private int buffID;
	private List<Npc> spawned = new LinkedList<>();

	public CustomBase(int id, NochsanaEvent ownerEvent, boolean siegeBoss, Race initialOwner, int buffID) {
		bossDeathListener = new CBaseDeathListener(this);
		this.siegeBoss = siegeBoss;
		this.owner = initialOwner;
		this.id = id;
		this.ownerEvent = ownerEvent;
		this.buffID = buffID;
		spawn();
	}

	public Npc getTarget() {
		return target;
	}

	public int getBuffId() {
		return buffID;
	}

	public boolean isTeleport() {
		return isTeleport;
	}

	public boolean isPreparingToSpawn() {
		if (this.bossRespawn == null) {
			return false;
		}

		if (this.bossRespawn.isCancelled()) {
			return false;
		}

		return true;
	}

	protected void spawn() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(owner)) {
					if (template.getHandlerType() != SpawnHandlerType.BOSS) {
						Npc npc = (Npc) ownerEvent.spawnObject(template.getNpcId(), template.getX(), template.getY(), template.getZ(), template.getHeading(),
							template.getRespawnTime());
						spawned.add(npc);

						if (template.getHandlerType() == SpawnHandlerType.FLAG) {
							flag = npc;
						}
						if (template.getHandlerType() == SpawnHandlerType.MERCHANT) {
							ownerEvent.assignTeleporterToBase(npc, this);
						}

						if (template.getHandlerType() == SpawnHandlerType.RIFT) {
							isTeleport = true;
							this.target = npc;
						}
					}
				}
			}
		}

		if (!siegeBoss) {
			scheduleBossRespawn();
		}

	}

	private List<SpawnGroup2> getBaseSpawns() {
		List<SpawnGroup2> spawns = DataManager.SPAWNS_DATA2.getBaseSpawnsByLocId(id);

		if (spawns == null) {
			throw new NullPointerException("No spawns for base:" + id);
		}
		return spawns;
	}

	public void scheduleBossRespawn() {
		bossRespawn = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				for (SpawnGroup2 group : getBaseSpawns()) {
					for (SpawnTemplate spawn : group.getSpawnTemplates()) {
						final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
						if (template.getBaseRace().equals(owner)) {
							if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.BOSS)) {
								Npc npc = (Npc) ownerEvent.spawnObject(template.getNpcId(), template.getX(), template.getY(), template.getZ(), template.getHeading(),
									0);
								boss = npc;
								addBossListeners();
							}
						}
					}
				}
				if (siegeBoss) {
					ownerEvent.announceAll("The Fortress Boss has Spawned.");
				}
			}
		}, 30 * 1000);
	}

	protected synchronized void despawn() {

		if (!bossRespawn.isDone()) {
			bossRespawn.cancel(false);
		}

		for (Npc npc : spawned) {
			if (npc != null) {
				if (npc.getSpawn().getHandlerType() != null || npc.getRace() != owner) {
					npc.getController().onDelete();
				}
			}
		}

		flag = null;

	}

	protected void deleteBoss() {
		if (!bossRespawn.isDone()) {
			if (bossRespawn.cancel(false))
				return;
		}
		AbstractAI ai = (AbstractAI) boss.getAi2();
		ai.removeEventListener(bossDeathListener);
		boss.getController().onDelete();
	}

	protected void addBossListeners() {
		AbstractAI ai = (AbstractAI) boss.getAi2();
		ai.addEventListener(bossDeathListener);
	}

	protected void capture(Race winner, String name, Player killer) {
		if (winner == owner) {
			BattleService.getInstance().log("Some Noobish GM (" + killer.getName() + ") is trying to destroy the Game");
			return;
		}
		try {
			lock.lock();
			boss = null;
			despawn();
			this.owner = winner;
			ownerEvent.announceAll(name + " of " + winner.name() + " captured a Base");
			ownerEvent.checkWorldCapture(this, winner);
			ownerEvent.onBaseCapture(killer);
			if (!siegeBoss) {
				spawn();
			}

		} catch (Exception ex) {
			BattleService.getInstance().log("Error:");
			ex.printStackTrace();
		}

		finally {
			lock.unlock();
		}

	}

	protected void capture(Race winner, String name, TemporaryPlayerTeam<?> team) {
		capture(winner, name, team.getLeaderObject());
	}

	protected Race getOwner() {
		return this.owner;
	}

	protected Npc getBoss() {
		return this.boss;
	}

	protected Npc getFlag() {
		return this.flag;
	}

	protected int getId() {
		return this.id;
	}

	@Override
	public int compareTo(CustomBase o) {
		if (o.getId() < this.getId()) {
			return -1;
		}
		if (o.getId() > this.getId()) {
			return 1;
		}
		return 0;
	}

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
				TemporaryPlayerTeam<?> team = (TemporaryPlayerTeam<?>) winner;
				if (team.getRace().isPlayerRace())
					base.capture(team.getRace(), team.getLeader().getName(), team);
			}
		}
	}
}
