package com.aionemu.gameserver.services.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javolution.util.FastTable;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.base.BaseLocation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Source
 */
public class Base<BL extends BaseLocation> {

	private Future<?> startAssault, stopAssault;
	private final BL baseLocation;
	private List<Race> list = new ArrayList<>();
	private final BossDeathListener bossDeathListener = new BossDeathListener(this);
	private List<Npc> attackers = new ArrayList<>();
	private final AtomicBoolean finished = new AtomicBoolean();
	private boolean started, is_new_map;
	private Npc boss, flag;
	private SM_SYSTEM_MESSAGE ending, killers;

	public Base(BL baseLocation) {
		list.add(Race.ASMODIANS);
		list.add(Race.ELYOS);
		list.add(Race.NPC);
		this.baseLocation = baseLocation;
		is_new_map = baseLocation.getWorldId() == 600090000 || baseLocation.getWorldId() == 600100000;
		switch (baseLocation.getId()) {
			case 6101:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V01();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V01();
				break;
			case 6102:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V02();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V02();
				break;
			case 6103:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V03();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V03();
				break;
			case 6104:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V04();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V04();
				break;
			case 6105:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V05();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V05();
				break;
			case 6106:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V06();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V06();
				break;
			case 6107:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V07();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V07();
				break;
			case 6108:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V08();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V08();
				break;
			case 6109:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V09();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V09();
				break;
			case 6110:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V10();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V10();
				break;
			case 6111:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V11();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V11();
				break;
			case 6112:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V12();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V12();
				break;
			case 6113:
				ending = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V13();
				killers = SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V13();
				break;
			default:
				ending = null;
				killers = null;
				break;
		}
	}

	public final void start() {

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

		spawn();
	}

	public final void stop() {
		if (finished.compareAndSet(false, true)) {
			if (getBoss() != null) {
				rmvBossListener();
			}
			despawn();
		}
	}

	private List<SpawnGroup2> getBaseSpawns() {
		List<SpawnGroup2> spawns = DataManager.SPAWNS_DATA2.getBaseSpawnsByLocId(getId());

		if (spawns == null) {
			throw new NullPointerException("No spawns for base:" + getId());
		}

		return spawns;
	}

	protected void spawn() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {
					if (template.getHandlerType() == null) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						if (npc.isFlag()) {
							setFlag(npc);
							MapRegion mr = npc.getPosition().getMapRegion();
							mr.activate();
						}
					}
				}
			}
		}

		if (ending != null) {
			World.getInstance().getWorldMap(getBaseLocation().getWorldId()).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {

				@Override
				public void visit(Player player) {
					PacketSendUtility.sendPacket(player, ending);
				}
			});
		}

		delayedAssault();
		delayedSpawn(getRace());
	}

	private void delayedAssault() {
		int delay = is_new_map ? Rnd.get(120, 360) : Rnd.get(15, 20);
		startAssault = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				chooseAttackersRace();
			}

		}, delay * 60000); // Randomly every 15 - 20 min start assault
	}

	private void delayedSpawn(final Race race) {
		int delay = is_new_map ? Rnd.get(2, 5) : 30;
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getRace().equals(race) && getBoss() == null) {
					spawnBoss();
				}
			}

		}, delay * 60000);
	}

	protected void spawnBoss() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {
					if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.BOSS)) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						setBoss(npc);
						addBossListeners();
					}
				}
			}
		}
	}

	protected void chooseAttackersRace() {
		AtomicBoolean next = new AtomicBoolean(Math.random() < 0.5);
		for (Race race : list) {
			if (!race.equals(getRace())) {
				if (next.compareAndSet(true, false)) {
					continue;
				}
				spawnAttackers(race);
				break;
			}
		}
	}

	public void spawnAttackers(Race race) {
		if (getFlag() == null) {
			throw new NullPointerException("Base:" + getId() + " flag is null!");
		} else if (!getFlag().getPosition().getMapRegion().isMapRegionActive()) {
			// 20% chance to capture base in not active region by invaders assault
			if (Math.random() < 0.2) {
				BaseService.getInstance().capture(getId(), race);
			} else {
				// Next attack
				delayedAssault();
			}
			return;
		}

		if (!isAttacked()) {
			despawnAttackers();

			for (SpawnGroup2 group : getBaseSpawns()) {
				for (SpawnTemplate spawn : group.getSpawnTemplates()) {
					final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
					if (template.getBaseRace().equals(race)) {
						if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.ATTACKER)) {
							Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
							getAttackers().add(npc);
						}
					}
				}
			}

			if (killers != null) {
				World.getInstance().getWorldMap(getBaseLocation().getWorldId()).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						PacketSendUtility.sendPacket(player, killers);
					}
				});
			}

			if (getAttackers().isEmpty()) {
				throw new NullPointerException("No attackers was found for base:" + getId());
			} else {
				stopAssault = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						despawnAttackers();

						// Next attack
						delayedAssault();
					}

				}, 5 * 60000); // After 5 min attackers despawned
			}
		}
	}

	public boolean isAttacked() {
		for (Npc attacker : getAttackers()) {
			if (!attacker.getLifeStats().isAlreadyDead()) {
				return true;
			}
		}
		return false;
	}

	protected void despawn() {
		setFlag(null);

		FastTable<Npc> spawned = World.getInstance().getBaseSpawns(getId());
		if (spawned != null) {
			for (Npc npc : spawned) {
				if (npc != null) {
					npc.getController().onDelete();
				}
			}
		}

		if (startAssault != null) {
			startAssault.cancel(true);
		}

		if (stopAssault != null) {
			stopAssault.cancel(true);
			despawnAttackers();
		}
	}

	protected void despawnAttackers() {
		for (Npc attacker : getAttackers()) {
			attacker.getController().onDelete();
		}
		getAttackers().clear();
	}

	protected void addBossListeners() {
		AbstractAI ai = (AbstractAI) getBoss().getAi2();
		ai.addEventListener(bossDeathListener);
	}

	protected void rmvBossListener() {
		AbstractAI ai = (AbstractAI) getBoss().getAi2();
		ai.removeEventListener(bossDeathListener);
	}

	public Npc getFlag() {
		return flag;
	}

	public void setFlag(Npc flag) {
		this.flag = flag;
	}

	public Npc getBoss() {
		return boss;
	}

	public void setBoss(Npc boss) {
		this.boss = boss;
	}

	public boolean isFinished() {
		return finished.get();
	}

	public BL getBaseLocation() {
		return baseLocation;
	}

	public int getId() {
		return baseLocation.getId();
	}

	public Race getRace() {
		return baseLocation.getRace();
	}

	public void setRace(Race race) {
		baseLocation.setRace(race);
	}

	public List<Npc> getAttackers() {
		return attackers;
	}

}
