package com.aionemu.gameserver.spawnengine;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.walker.RouteStep;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;

/**
 * Stores for the spawn needed information, used for forming walker groups and spawning NPCs
 * 
 * @author vlog, Rolandas
 */
public class ClusteredNpc {

	private Npc npc;
	private int instance;
	private WalkerTemplate walkTemplate;
	private float x;
	private float y;

	public ClusteredNpc(Npc npc, int instance, WalkerTemplate walkTemplate) {
		this.npc = npc;
		this.instance = instance;
		this.walkTemplate = walkTemplate;
		this.x = npc.getSpawn().getX();
		this.y = npc.getSpawn().getY();
	}

	public Npc getNpc() {
		return npc;
	}

	public int getInstance() {
		return instance;
	}

	public void spawn(float z) {
		SpawnEngine.bringIntoWorld(npc, npc.getSpawn().getWorldId(), instance, x, y, z, npc.getSpawn().getHeading());
	}

	public void despawn() {
		npc.getMoveController().abortMove();
		npc.getController().deleteIfAliveOrCancelRespawn();
	}

	public void setNpc(Npc npc, RouteStep step) {
		npc.setWalkerGroupShift(this.npc.getWalkerGroupShift());
		this.npc = npc;
		this.x = step.getX();
		this.y = step.getY();
	}

	public boolean hasSamePosition(ClusteredNpc other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		return this.x == other.x && this.y == other.y;
	}

	public int getPositionHash() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}

	public float getXDelta() {
		return walkTemplate.getRouteStep(0).getX() - x;
	}

	/**
	 * @param x
	 *          the x to set
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}

	public float getYDelta() {
		return walkTemplate.getRouteStep(0).getY() - y;
	}

	/**
	 * @param y
	 *          the y to set
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * @return the walkTemplate
	 */
	public WalkerTemplate getWalkTemplate() {
		return walkTemplate;
	}

	public Integer getWalkerIndex() {
		return npc.getSpawn().getWalkerIndex();
	}

}
