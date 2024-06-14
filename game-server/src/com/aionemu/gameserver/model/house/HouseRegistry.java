package com.aionemu.gameserver.model.house;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.dao.PlayerRegisteredItemsDAO;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.templates.housing.PartType;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class HouseRegistry implements Persistable {

	private final House owner;
	private final Map<Integer, HouseObject<?>> objects = new LinkedHashMap<>();
	private final Map<Integer, HouseDecoration> decors = new LinkedHashMap<>();
	private PersistentState persistentState = PersistentState.UPDATED;

	public HouseRegistry(House owner) {
		this.owner = owner;
	}

	public House getOwner() {
		return owner;
	}

	/**
	 * @return All objects including deleted
	 */
	public List<HouseObject<?>> getObjects() {
		return new ArrayList<>(objects.values());
	}

	public List<HouseObject<?>> getSpawnedObjects() {
		List<HouseObject<?>> temp = new ArrayList<>();
		for (HouseObject<?> obj : objects.values()) {
			if (obj.isSpawnedByPlayer() && obj.getPersistentState() != PersistentState.DELETED)
				temp.add(obj);
		}
		return temp;
	}

	public List<HouseObject<?>> getNotSpawnedObjects() {
		List<HouseObject<?>> temp = new ArrayList<>();
		for (HouseObject<?> obj : objects.values()) {
			if (!obj.isSpawnedByPlayer() && obj.getPersistentState() != PersistentState.DELETED)
				temp.add(obj);
		}
		return temp;
	}

	public HouseObject<?> getObjectByObjId(int itemObjId) {
		return objects.get(itemObjId);
	}

	public boolean putObject(HouseObject<?> houseObject, boolean saveRegistry) {
		if (objects.putIfAbsent(houseObject.getObjectId(), houseObject) != null)
			return false;
		if (houseObject.getPersistentState() != PersistentState.UPDATED) // state is UPDATED when reloading registry and spawned objects get reused
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		if (saveRegistry)
			save();
		return true;
	}

	public void discardObject(HouseObject<?> object, boolean direct) {
		discard(objects, object, direct);
	}

	public List<HouseDecoration> getDecors() {
		return new ArrayList<>(decors.values());
	}

	public List<HouseDecoration> getUnusedDecors() {
		List<HouseDecoration> temp = new ArrayList<>();
		for (HouseDecoration decor : decors.values()) {
			if (decor.getPersistentState() != PersistentState.DELETED && decor.getRoom() == -1)
				temp.add(decor);
		}
		return temp;
	}

	public HouseDecoration getDecorByObjId(int itemObjId) {
		return decors.get(itemObjId);
	}

	public boolean putDecor(HouseDecoration decor, boolean saveRegistry) {
		if (decors.putIfAbsent(decor.getObjectId(), decor) != null)
			return false;
		if (decor.getPersistentState() != PersistentState.UPDATED)
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		if (saveRegistry)
			save();
		return true;
	}

	public Integer getUsedDecorId(PartType partType, int room) {
		for (HouseDecoration decor : decors.values()) {
			if (decor.getPersistentState() != PersistentState.DELETED && decor.getTemplate().getType() == partType && decor.getRoom() == room)
				return decor.getTemplateId();
		}
		return getOwner().getBuilding().getDefaultDecorId(partType);
	}

	public void setUsed(HouseDecoration decor, int room) {
		if (decor.getPersistentState() == PersistentState.DELETED || decor.getRoom() == room)
			return;
		discardDecor(decor.getTemplate().getType(), room);
		Integer defaultPartId = getOwner().getBuilding().getDefaultDecorId(decor.getTemplate().getType());
		if (defaultPartId == decor.getTemplateId()) {
			decor.setPersistentState(PersistentState.DELETED);
		} else {
			decor.setRoom(room);
			if (decor.getPersistentState() != PersistentState.NEW) {
				decor.setPersistentState(PersistentState.UPDATE_REQUIRED);
				setPersistentState(PersistentState.UPDATE_REQUIRED);
			}
		}
	}

	public void discardDecor(PartType partType, int roomNo) {
		for (HouseDecoration decor : getDecors()) {
			if (decor.getTemplate().getType() == partType && decor.getRoom() == roomNo)
				discardDecor(decor, false);
		}
	}

	public void discardDecor(HouseDecoration decor, boolean direct) {
		discard(decors, decor, direct);
	}

	private <T extends AionObject & Persistable> void discard(Map<Integer, T> map, T obj, boolean direct) {
		if (obj.getPersistentState() == PersistentState.NEW || direct) {
			map.remove(obj.getObjectId(), obj);
		} else {
			obj.setPersistentState(PersistentState.DELETED);
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
		// remove house object use cooldowns for this object
		if (obj instanceof UseableHouseObject<?> useableHouseObject && useableHouseObject.hasUseCooldown())
			World.getInstance().forEachPlayer(player -> player.getHouseObjectCooldowns().remove(obj.getObjectId()));
	}

	/**
	 * Despawns all objects and updates DB.
	 */
	public void reset() {
		List<HouseObject<?>> spawnedObjects = getSpawnedObjects();
		if (spawnedObjects.isEmpty()) {
			if (getOwner().getOwnerId() != 0)
				PlayerRegisteredItemsDAO.resetRegistry(getOwner().getOwnerId());
		} else {
			for (HouseObject<?> obj : spawnedObjects)
				obj.removeFromHouse();
		}
		for (HouseDecoration decor : decors.values()) {
			if (decor.getRoom() != -1)
				discardDecor(decor, false);
		}
		save();
	}

	public void save() {
		if (persistentState == PersistentState.UPDATE_REQUIRED)
			PlayerRegisteredItemsDAO.store(this, getOwner().getOwnerId());
	}

	@Override
	public final PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	public final void setPersistentState(PersistentState persistentState) {
		this.persistentState = persistentState;
	}

	public int size() {
		return objects.size() + decors.size();
	}
}
