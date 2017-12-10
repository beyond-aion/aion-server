package com.aionemu.gameserver.model.gameobjects.player;

import com.aionemu.gameserver.model.gameobjects.Persistable;

/**
 * @author evilset
 */
public class BindPointPosition implements Persistable {

	private int mapId;
	private float x;
	private float y;
	private float z;
	private byte heading;
	private PersistentState persistentState;

	/**
	 * @param mapId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 */
	public BindPointPosition(int mapId, float x, float y, float z, byte heading) {
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.persistentState = PersistentState.NEW;
	}

	/**
	 * @return Returns the mapId.
	 */
	public int getMapId() {
		return mapId;
	}

	/**
	 * @return Returns the x.
	 */
	public float getX() {
		return x;
	}

	/**
	 * @return Returns the y.
	 */
	public float getY() {
		return y;
	}

	/**
	 * @return Returns the z.
	 */
	public float getZ() {
		return z;
	}

	/**
	 * @return Returns the heading.
	 */
	public byte getHeading() {
		return heading;
	}

	/**
	 * @return the persistentState
	 */
	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	/**
	 * @param persistentState
	 *          the persistentState to set
	 */
	@Override
	@SuppressWarnings("fallthrough")
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case UPDATE_REQUIRED:
				if (this.persistentState == PersistentState.NEW)
					break;
			default:
				this.persistentState = persistentState;
		}
	}
}
