package com.aionemu.gameserver.services.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.base.BaseLocation;
import com.aionemu.gameserver.model.base.StainedBaseLocation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Source, Estrayl
 */
public abstract class Base<T extends BaseLocation> {

	private final T bLoc;
	private final int id;
	private List<Npc> assaulter = new ArrayList<>();
	private Future<?> assaultTask, assaultDespawnTask, bossSpawnTask, enhancedSpawnTask, outriderSpawnTask;
	private AtomicBoolean isStarted = new AtomicBoolean();
	private AtomicBoolean isFinished = new AtomicBoolean();
	private Npc boss, flag;

	protected abstract int getAssaultDelay();

	protected abstract int getAssaultDespawnDelay();

	protected abstract int getBossSpawnDelay();

	protected abstract int getNpcSpawnDelay();

	Base(T bLoc) {
		this.bLoc = bLoc;
		this.id = bLoc.getId();
	}

	public final void start() throws BaseException {
		if (isStarted.compareAndSet(false, true))
			handleStart();
		else
			throw new BaseException("Attempt to start Base twice! ID:" + id);
	}

	public final void stop() throws BaseException {
		if (isFinished.compareAndSet(false, true))
			handleStop();
		else
			throw new BaseException("Attempt to stop Base twice! ID:" + id);
	}

	protected void handleStart() {
		spawnBySpawnHandler(SpawnHandlerType.FLAG, getRace());
		spawnBySpawnHandler(SpawnHandlerType.MERCHANT, getRace());
		spawnBySpawnHandler(SpawnHandlerType.SENTINEL, getRace());
		scheduleOutriderSpawn();
		scheduleBossSpawn();
	}

	protected void handleStop() {
		cancelTask(assaultTask, assaultDespawnTask, bossSpawnTask, enhancedSpawnTask, outriderSpawnTask);
		despawnAllNpcs();
	}

	private void despawnAllNpcs() {
		List<Npc> spawned = World.getInstance().getBaseSpawns(id);
		if (spawned != null) {
			for (Npc npc : spawned) {
				if (npc != null)
					npc.getController().deleteIfAliveOrCancelRespawn();
			}
		}
	}

	protected void scheduleOutriderSpawn() {
		outriderSpawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isFinished.get() || getNpcSpawnDelay() == 0)
				return;
			spawnBySpawnHandler(SpawnHandlerType.OUTRIDER, getRace());
			if (bLoc instanceof StainedBaseLocation) {
				if (((StainedBaseLocation) bLoc).isEnhanced()) {
					spawnBySpawnHandler(SpawnHandlerType.GUARDIAN, getRace());
					spawnBySpawnHandler(SpawnHandlerType.OUTRIDER_ENHANCED, getRace());
				}
			}
		}, getNpcSpawnDelay());
	}

	protected void scheduleBossSpawn() {
		bossSpawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isFinished.get())
				return;
			spawnBySpawnHandler(SpawnHandlerType.BOSS, getRace());
			SM_SYSTEM_MESSAGE bossSpawnMsg = getBossSpawnMsg();
			if (bossSpawnMsg != null)
				PacketSendUtility.broadcastToMap(flag.getPosition().getWorldMapInstance(), bossSpawnMsg);
			scheduleAssault();
		}, getBossSpawnDelay());
	}

	private void scheduleAssault() {
		assaultTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isFinished.get())
				return;
			if (flag.getPosition().isMapRegionActive()) {
				spawnBySpawnHandler(SpawnHandlerType.ATTACKER, chooseAssaultRace());
				SM_SYSTEM_MESSAGE assaultMsg = getAssaultMsg();
				if (assaultMsg != null)
					PacketSendUtility.broadcastToMap(flag.getPosition().getWorldMapInstance(), assaultMsg);
				scheduleAssaultDespawn();
			} else {
				if (Rnd.chance() < 20)
					BaseService.getInstance().capture(id, chooseAssaultRace());
				scheduleAssault();
			}
		}, getAssaultDelay());
	}

	public void scheduleEnhancedSpawns() {
		enhancedSpawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isFinished.get())
				return;
			despawnByHandlerType(SpawnHandlerType.GUARDIAN); // prevents double spawns
			despawnByHandlerType(SpawnHandlerType.OUTRIDER_ENHANCED);
			spawnBySpawnHandler(SpawnHandlerType.GUARDIAN, getRace());
			spawnBySpawnHandler(SpawnHandlerType.OUTRIDER_ENHANCED, getRace());
		}, 295 * 1000);
	}

	private Race chooseAssaultRace() {
		List<Race> coll = new ArrayList<>();
		coll.add(Race.ASMODIANS);
		coll.add(Race.ELYOS);
		coll.add(Race.NPC);
		coll.remove(getRace());
		return coll.get(Rnd.get(0, 1));
	}

	private void scheduleAssaultDespawn() {
		assaultDespawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isFinished.get())
				return;
			despawnAssaulter();
			scheduleAssault();
		}, getAssaultDespawnDelay());
	}

	private void despawnAssaulter() {
		for (Npc npc : assaulter) {
			if (npc != null)
				npc.getController().deleteIfAliveOrCancelRespawn();
		}
		assaulter.clear();
	}

	public void spawnBySpawnHandler(SpawnHandlerType type, Race targetRace) {
		for (SpawnGroup group : DataManager.SPAWNS_DATA.getBaseSpawnsByLocId(id)) {
			if (group.getHandlerType() != type)
				continue;
			for (SpawnTemplate template : group.getSpawnTemplates()) {
				if (((BaseSpawnTemplate) template).getBaseRace() != targetRace)
					continue;
				Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
				switch (type) {
					case ATTACKER:
						assaulter.add(npc);
						break;
					case BOSS:
						initBoss(npc);
						break;
					case FLAG:
						initFlag(npc);
						break;
				}
			}
		}
		if (type == SpawnHandlerType.BOSS && boss == null)
			throw new BaseException("No boss found for base! ID: " + id);
		if (type == SpawnHandlerType.FLAG && flag == null)
			throw new BaseException("No flag found for base! ID: " + id);
	}

	private void initBoss(Npc npc) throws BaseException, NullPointerException {
		if (npc == null)
			throw new BaseException("Boss could not be spawned! Base ID: " + id);
		if (boss != null)
			throw new BaseException("Tried to initialize boss twice! Base ID: " + id);
		boss = npc;
		boss.getAi().addEventListener(new BaseBossDeathListener(this));
	}

	private void initFlag(Npc npc) throws BaseException, NullPointerException {
		if (npc == null)
			throw new BaseException("Flag could not be spawned! Base ID: " + id);
		if (flag != null)
			throw new BaseException("Tried to initialize flag twice! Base ID: " + id);
		flag = npc;
	}

	/**
	 * Despawns all alive npcs of this base, which have the given SpawnHandlerType. Respawn tasks of dead base npcs will be cancelled.
	 */
	public void despawnByHandlerType(SpawnHandlerType type) {
		for (Npc npc : World.getInstance().getBaseSpawns(id)) {
			if (npc != null && npc.getSpawn().getHandlerType() == type) {
				npc.getController().deleteIfAliveOrCancelRespawn();
			}
		}
	}

	private SM_SYSTEM_MESSAGE getBossSpawnMsg() {
		return switch (id) {
			case 6101 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V01();
			case 6102 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V02();
			case 6103 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V03();
			case 6104 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V04();
			case 6105 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V05();
			case 6106 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V06();
			case 6107 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V07();
			case 6108 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V08();
			case 6109 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V09();
			case 6110 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V10();
			case 6111 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V11();
			case 6112 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V12();
			case 6113 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_CHIEF_V13();
			default -> null;
		};
	}

	private SM_SYSTEM_MESSAGE getAssaultMsg() {
		return switch (id) {
			case 6101 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V01();
			case 6102 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V02();
			case 6103 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V03();
			case 6104 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V04();
			case 6105 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V05();
			case 6106 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V06();
			case 6107 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V07();
			case 6108 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V08();
			case 6109 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V09();
			case 6110 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V10();
			case 6111 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V11();
			case 6112 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V12();
			case 6113 -> SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_KILLER_V13();
			default -> null;
		};
	}

	/**
	 * @param tasks
	 *          - can be null if the base will be captured with command or under npc control
	 */
	protected void cancelTask(Future<?>... tasks) {
		for (Future<?> task : tasks) {
			if (task != null && !task.isDone())
				task.cancel(true);
		}
	}

	public Npc getBoss() {
		return boss;
	}

	public Npc getFlag() {
		return flag;
	}

	public T getLocation() {
		return bLoc;
	}

	public int getId() {
		return id;
	}

	public int getWorldId() {
		return bLoc.getWorldId();
	}

	public Race getRace() {
		return bLoc.getRace();
	}

	public void setLocRace(Race race) {
		bLoc.setRace(race);
	}

	public boolean isStarted() {
		return isStarted.get();
	}

	public boolean isFinished() {
		return isFinished.get();
	}

	public boolean isUnderAssault() {
		return !assaulter.isEmpty();
	}
}
