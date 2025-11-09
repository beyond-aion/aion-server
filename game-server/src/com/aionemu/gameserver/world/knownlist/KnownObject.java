package com.aionemu.gameserver.world.knownlist;

import java.util.Objects;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;

public class KnownObject {

	private final VisibleObject object;
	private boolean visible;

	public KnownObject(VisibleObject object) {
		this.object = object;
	}

	public VisibleObject get() {
		return object;
	}

	public boolean isVisible() {
		return visible;
	}

	boolean updateVisible(boolean visible) {
		synchronized (this) {
			if (this.visible != visible) {
				this.visible = visible;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		KnownObject that = (KnownObject) o;
		return Objects.equals(object, that.object);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(object);
	}

	@Override
	public String toString() {
		return object.getName() + " (objectId: " + object.getObjectId() + ", visible: " + visible +")";
	}
}
