package com.aionemu.gameserver.model.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
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
	private final List<Npc> assaulter = new ArrayList<>();
	private final AtomicBoolean isStarted = new AtomicBoolean();
	private final AtomicBoolean isStopped = new AtomicBoolean();
	private Future<?> assaultTask, assaultDespawnTask, bossSpawnTask, outriderSpawnTask;
	private Npc flag;

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
		if (isStopped.compareAndSet(false, true))
			handleStop();
		else
			throw new BaseException("Attempt to stop Base twice! ID:" + id);
	}

	protected void handleStart() {
		spawnBySpawnHandler(SpawnHandlerType.FLAG, getOccupier());
		spawnBySpawnHandler(SpawnHandlerType.MERCHANT, getOccupier());
		spawnBySpawnHandler(SpawnHandlerType.SENTINEL, getOccupier());
		scheduleOutriderSpawn();
		scheduleBossSpawn();
	}

	protected void handleStop() {
		cancelTask(assaultTask, assaultDespawnTask, bossSpawnTask, outriderSpawnTask);
		despawnAllNpcs();
	}

	private void despawnAllNpcs() {
		despawnNpcs(null);
	}

	protected void despawnByHandlerType(SpawnHandlerType type) {
		despawnNpcs(type);
	}

	private void despawnNpcs(SpawnHandlerType type) {
		World.getInstance().getWorldMap(getLocation().getTemplate().getWorldId()).forEachObject(o -> {
			if (o.getSpawn() instanceof BaseSpawnTemplate spawn && spawn.getId() == id && (type == null || spawn.getHandlerType() == type))
				o.getController().deleteIfAliveOrCancelRespawn();
		});
	}

	protected void scheduleOutriderSpawn() {
		outriderSpawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isStopped.get() || getNpcSpawnDelay() == 0)
				return;
			spawnBySpawnHandler(SpawnHandlerType.OUTRIDER, getOccupier());
		}, getNpcSpawnDelay());
	}

	protected void scheduleBossSpawn() {
		if (bLoc.getOccupier() == BaseOccupier.PEACE)
			return; // Peace does not include any boss or the possibility to capture it

		bossSpawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isStopped.get())
				return;
			spawnBySpawnHandler(SpawnHandlerType.BOSS, getOccupier());
			SM_SYSTEM_MESSAGE bossSpawnMsg = getBossSpawnMsg();
			if (bossSpawnMsg != null)
				PacketSendUtility.broadcastToMap(flag.getPosition().getWorldMapInstance(), bossSpawnMsg);
			scheduleAssault();
		}, getBossSpawnDelay());
	}

	private void scheduleAssault() {
		if (bLoc.getType() == BaseType.PANESTERRA_FACTION_CAMP || bLoc.getType() == BaseType.PANESTERRA_ARTIFACT)
			return; // No assault for those two

		assaultTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isStopped.get())
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

	protected BaseOccupier chooseAssaultRace() {
		List<BaseOccupier> coll = new ArrayList<>(List.of(BaseOccupier.ASMODIANS, BaseOccupier.ELYOS, BaseOccupier.BALAUR));
		coll.remove(getOccupier());
		return Rnd.get(coll);
	}

	private void scheduleAssaultDespawn() {
		assaultDespawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isStopped.get())
				return;
			despawnAssaulter();
			scheduleAssault();
		}, getAssaultDespawnDelay());
	}

	private void despawnAssaulter() {
		for (Npc npc : assaulter)
			npc.getController().deleteIfAliveOrCancelRespawn();
		assaulter.clear();
	}

	public void spawnBySpawnHandler(SpawnHandlerType type, BaseOccupier occupier) {
		Npc boss = null;
		for (SpawnGroup group : DataManager.SPAWNS_DATA.getBaseSpawnsByLocId(id)) {
			if (group.getHandlerType() != type)
				continue;
			for (SpawnTemplate template : group.getSpawnTemplates()) {
				if (((BaseSpawnTemplate) template).getOccupier() != occupier)
					continue;
				Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
				if (npc == null)
					throw new BaseException("Npc " + template.getNpcId() + " could not be spawned at base " + id);
				switch (type) {
					case ATTACKER:
						assaulter.add(npc);
						break;
					case BOSS:
						if (boss != null)
							throw new BaseException("Tried to spawn boss twice at base " + id);
						boss = npc;
						break;
					case FLAG:
						if (flag != null)
							throw new BaseException("Tried to spawn flag twice at base " + id);
						flag = npc;
						break;
				}
			}
		}
		if (type == SpawnHandlerType.BOSS && boss == null)
			throw new BaseException("No boss found for base! ID: " + id);
		if (type == SpawnHandlerType.FLAG && flag == null)
			throw new BaseException("No flag found for base! ID: " + id);
	}

	public BaseOccupier getOccupier(Creature bossKiller) {
		return bossKiller == null ? getLocation().getTemplate().getDefaultOccupier() : BaseOccupier.findBy(bossKiller.getRace());
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
	 *          - can be null if the base is captured with command or under npc control
	 */
	protected void cancelTask(Future<?>... tasks) {
		for (Future<?> task : tasks) {
			if (task != null && !task.isDone())
				task.cancel(true);
		}
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

	public BaseOccupier getOccupier() {
		return bLoc.getOccupier();
	}

	public boolean isStarted() {
		return isStarted.get();
	}

	public boolean isStopped() {
		return isStopped.get();
	}

	public boolean isUnderAssault() {
		return !assaulter.isEmpty();
	}
}
