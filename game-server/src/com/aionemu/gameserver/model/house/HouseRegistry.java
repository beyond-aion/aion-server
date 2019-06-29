package com.aionemu.gameserver.model.house;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerRegisteredItemsDAO;
import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.templates.housing.PartType;

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

	public boolean putObject(HouseObject<?> houseObject) {
		if (objects.containsKey(houseObject.getObjectId()))
			return false;

		objects.put(houseObject.getObjectId(), houseObject);
		if (houseObject.getPersistentState() != PersistentState.UPDATED) // state is UPDATED when reloading registry and spawned objects get reused
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		return true;
	}

	public HouseObject<?> removeObject(int itemObjId) {
		if (!objects.containsKey(itemObjId))
			return null;

		HouseObject<?> oldObject = objects.get(itemObjId);
		if (oldObject.getPersistentState() == PersistentState.NEW)
			discardObject(itemObjId);
		else
			oldObject.setPersistentState(PersistentState.DELETED);
		setPersistentState(PersistentState.UPDATE_REQUIRED);

		return oldObject;
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

	public boolean putDecor(HouseDecoration decor) {
		if (decors.containsKey(decor.getObjectId()))
			return false;

		decors.put(decor.getObjectId(), decor);
		if (decor.getPersistentState() != PersistentState.UPDATED)
			setPersistentState(PersistentState.UPDATE_REQUIRED);
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
			if (decor.getTemplate().getType() == partType && decor.getRoom() == roomNo) {
				if (decor.getPersistentState() == PersistentState.NEW) {
					discardDecor(decor);
				} else {
					decor.setPersistentState(PersistentState.DELETED);
					setPersistentState(PersistentState.UPDATE_REQUIRED);
				}
			}
		}
	}

	public void discardObject(int objectId) {
		objects.remove(objectId);
	}

	public void discardDecor(HouseDecoration decor) {
		decors.remove(decor.getObjectId());
	}

	/**
	 * Despawns all objects and updates DB.
	 */
	public void reset() {
		List<HouseObject<?>> spawnedObjects = getSpawnedObjects();
		if (spawnedObjects.isEmpty()) {
			if (getOwner().getOwnerId() != 0)
				DAOManager.getDAO(PlayerRegisteredItemsDAO.class).resetRegistry(getOwner().getOwnerId());
		} else {
			for (HouseObject<?> obj : spawnedObjects)
				obj.removeFromHouse();
			save();
		}
	}

	public void save() {
		if (persistentState == PersistentState.UPDATE_REQUIRED)
			DAOManager.getDAO(PlayerRegisteredItemsDAO.class).store(this, getOwner().getOwnerId());
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
