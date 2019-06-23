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
	private final Map<Integer, HouseDecoration> customParts = new LinkedHashMap<>();
	private final HouseDecoration[] defaultParts = new HouseDecoration[28];
	private PersistentState persistentState = PersistentState.UPDATED;

	public HouseRegistry(House owner) {
		this.owner = owner;
		putDefaultParts();
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

	/**
	 * In packets used custom parts are not included (kinda semi-deleted)
	 */
	public List<HouseDecoration> getCustomParts() {
		List<HouseDecoration> temp = new ArrayList<>();
		for (HouseDecoration decor : customParts.values()) {
			if (decor.getPersistentState() != PersistentState.DELETED && !decor.isUsed())
				temp.add(decor);
		}
		return temp;
	}

	public HouseDecoration getCustomPartByType(PartType partType, int room) {
		for (HouseDecoration deco : customParts.values()) {
			if (deco.getPersistentState() != PersistentState.DELETED && deco.getTemplate().getType() == partType) {
				if (room == deco.getRoom())
					return deco;
			}
		}
		return null;
	}

	public HouseDecoration getCustomPartByObjId(int itemObjId) {
		return customParts.get(itemObjId);
	}

	public HouseDecoration getCustomPartByPartId(int partId, int room) {
		for (HouseDecoration deco : customParts.values()) {
			if (deco.getPersistentState() != PersistentState.DELETED && deco.getTemplate().getId() == partId && deco.getRoom() == room) {
				return deco;
			}
		}
		return null;
	}

	public int getCustomPartCountByPartId(int partId) {
		int counter = 0;
		for (HouseDecoration deco : customParts.values()) {
			if (deco.getPersistentState() != PersistentState.DELETED && deco.getTemplate().getId() == partId) {
				counter++;
			}
		}
		return counter;
	}

	public boolean putCustomPart(HouseDecoration houseDeco) {
		if (customParts.containsKey(houseDeco.getObjectId()))
			return false;

		customParts.put(houseDeco.getObjectId(), houseDeco);
		if (houseDeco.getPersistentState() != PersistentState.UPDATED)
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		return true;
	}

	public HouseDecoration removeCustomPart(int itemObjId) {
		HouseDecoration obj = null;

		if (customParts.containsKey(itemObjId)) {
			obj = customParts.get(itemObjId);
			obj.setPersistentState(PersistentState.DELETED);
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
		return obj;
	}

	public List<HouseDecoration> getDefaultParts() {
		List<HouseDecoration> temp = new ArrayList<>();
		for (HouseDecoration deco : defaultParts) {
			if (deco != null)
				temp.add(deco);
		}
		return temp;
	}

	/**
	 * @return Default decoration for the part type
	 */
	public HouseDecoration getDefaultPartByType(PartType partType, int room) {
		return defaultParts[partType.getStartLineNr() + room];
	}

	private void putDefaultParts() {
		for (PartType partType : PartType.values()) {
			Integer partId = owner.getBuilding().getDefaultPartId(partType);
			if (partId == null)
				continue;
			for (int line = partType.getStartLineNr(); line <= partType.getEndLineNr(); line++) {
				int room = partType.getEndLineNr() - line;
				HouseDecoration decor = new HouseDecoration(0, partId, room);
				putDefaultPart(decor, room);
			}
		}
	}

	private void putDefaultPart(HouseDecoration houseDeco, int room) {
		defaultParts[houseDeco.getTemplate().getType().getStartLineNr() + room] = houseDeco;
		houseDeco.setPersistentState(PersistentState.NOACTION);
	}

	/**
	 * @return All decoration parts including deleted
	 */
	public List<HouseDecoration> getAllParts() {
		List<HouseDecoration> temp = new ArrayList<>();
		for (HouseDecoration deco : defaultParts) {
			if (deco != null)
				temp.add(deco);
		}
		temp.addAll(customParts.values());
		return temp;
	}

	public HouseDecoration getRenderPart(PartType partType, int room) {
		for (HouseDecoration decor : customParts.values()) {
			if (decor.getTemplate().getType() == partType && decor.isUsed() && decor.getRoom() == room) {
				return decor;
			}
		}
		return getDefaultPartByType(partType, room);
	}

	public void setPartInUse(HouseDecoration decorationUse, int room) {
		HouseDecoration defaultDecor = defaultParts[decorationUse.getTemplate().getType().getStartLineNr() + room];
		if (defaultDecor.getTemplate().getId() == decorationUse.getTemplate().getId()) {
			defaultDecor.setUsed(true);
			for (HouseDecoration decor : customParts.values()) {
				if (decor.getTemplate().getType() != decorationUse.getTemplate().getType())
					continue;
				if (decor.getPersistentState() == PersistentState.DELETED)
					continue;
				if (decor.isUsed() && decor.getRoom() == room) {
					decor.setUsed(false);
					decor.setRoom(-1);
					if (decor.getPersistentState() == PersistentState.NEW)
						discardPart(decor);
					else
						decor.setPersistentState(PersistentState.DELETED);
				}
			}
			return;
		}
		for (HouseDecoration decor : customParts.values()) {
			if (decor.getTemplate().getType() != decorationUse.getTemplate().getType())
				continue;
			if (decor.getPersistentState() == PersistentState.DELETED)
				continue;
			if (decorationUse.equals(decor)) {
				decor.setUsed(true);
				decor.setRoom(room);
				defaultDecor.setUsed(false);
			} else if (decor.isUsed() && decor.getRoom() == room) {
				decor.setUsed(false);
				decor.setRoom(-1);
				if (decor.getPersistentState() == PersistentState.NEW)
					discardPart(decor);
				else
					decor.setPersistentState(PersistentState.DELETED);
			}
		}
	}

	public void discardObject(int objectId) {
		objects.remove(objectId);
	}

	public void discardPart(HouseDecoration decor) {
		customParts.remove(decor.getObjectId());
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
		return objects.size() + customParts.size();
	}
}
