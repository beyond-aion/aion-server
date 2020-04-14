package com.aionemu.gameserver.model.rift;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.rift.RiftTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author Source
 */
public class RiftLocation {

	private boolean opened;
	private final RiftTemplate template;
	private Map<Integer, SpawnTemplate> spawned = new ConcurrentHashMap<>();

	public RiftLocation(RiftTemplate template) {
		this.template = template;
	}

	public int getId() {
		return template.getId();
	}

	public int getWorldId() {
		return template.getWorldId();
	}

	public boolean hasSpawns() {
		return template.hasSpawns();
	}

	public boolean isAutoCloseable() {
		return template.isAutoCloseable();
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean state) {
		opened = state;
	}

	public Map<Integer, SpawnTemplate> getSpawned() {
		return spawned;
	}

	public void addSpawned(VisibleObject object) {
		spawned.put(object.getObjectId(), object.getSpawn());
	}

	public boolean replaceSpawned(int oldObjectId, VisibleObject newObject) {
		if (spawned.remove(oldObjectId) != null) {
			addSpawned(newObject);
			return true;
		}
		return false;
	}
}
