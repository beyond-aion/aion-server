package com.aionemu.gameserver.custom.nochsanapvp;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.custom.BattleService;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.stats.NpcStatsTemplate;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;

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

	public CustomBase(int id, NochsanaEvent ownerEvent, boolean siegeBoss, Race initialOwner) {
		bossDeathListener = new CBaseDeathListener(this);
		this.siegeBoss = siegeBoss;
		this.owner = initialOwner;
		this.id = id;
		this.ownerEvent = ownerEvent;
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				spawn();
			}
		}, 10 * 1000);
	}

	public Npc getTarget() {
		return target;
	}

	public boolean isTeleport() {
		return isTeleport;
	}

	public boolean isPreparingToSpawn() {
		return bossRespawn != null;
	}

	protected void spawn() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(owner)) {
					if (template.getHandlerType() != SpawnHandlerType.BOSS) {
						Npc npc = (Npc) ownerEvent.spawnObject(template.getNpcId(), template.getX(), template.getY(), template.getZ(), template.getHeading(),
							template.getRespawnTime());
						NpcTemplate npcTemplate = npc.getObjectTemplate();
						changeNpcStats(npcTemplate, npc);
						if (npcTemplate.getNpcTemplateType().equals(NpcTemplateType.FLAG)) {
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

	private int getNewValue(int value, Npc npc) {
		return (value / npc.getLevel()) * ownerEvent.getNpcLevel(npc);
	}

	private void changeNpcStats(NpcTemplate template, Npc npc) {
		NpcStatsTemplate stats = template.getStatsTemplate();
		stats.setAccuracy(getNewValue((int) stats.getAccuracy(), npc));
		stats.setBlock(getNewValue((int) stats.getBlock(), npc));
		stats.setCrit(getNewValue((int) stats.getCrit(), npc));
		stats.setEvasion(getNewValue((int) stats.getEvasion(), npc));
		stats.setMaxHp(getNewValue((int) stats.getMaxHp(), npc));
		stats.setMaxMp(getNewValue((int) stats.getMaxMp(), npc));
		stats.setMaxXp(getNewValue((int) stats.getMaxXp(), npc));
		stats.setMdef(getNewValue((int) stats.getMdef(), npc));
		stats.setMresist(getNewValue((int) stats.getMresist(), npc));
		stats.setParry(getNewValue((int) stats.getParry(), npc));
		stats.setPdef(getNewValue((int) stats.getPdef(), npc));
		stats.setPower(getNewValue((int) stats.getPower(), npc));

		template.setLevel(ownerEvent.getNpcLevel(npc));

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
								NpcTemplate npcTemplate = npc.getObjectTemplate();
								changeNpcStats(npcTemplate, npc);
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

	protected void despawn() {

		if (!bossRespawn.isDone()) {
			bossRespawn.cancel(false);
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
			ownerEvent.announceAll(name + " of " + winner.name() + " Captured a Base");
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

}
