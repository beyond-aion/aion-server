package com.aionemu.gameserver.model.assemblednpc;

import javolution.util.FastTable;

/**
 * @author xTz
 */
public class AssembledNpc {

	private FastTable<AssembledNpcPart> assembledPatrs = new FastTable<AssembledNpcPart>();
	private long spawnTime = System.currentTimeMillis();
	private int routeId;
	private int mapId;

	public AssembledNpc(int routeId, int mapId, int liveTime, FastTable<AssembledNpcPart> assembledPatrs) {
		this.assembledPatrs = assembledPatrs;
		this.routeId = routeId;
		this.mapId = mapId;
	}

	public FastTable<AssembledNpcPart> getAssembledParts() {
		return assembledPatrs;
	}

	public int getRouteId() {
		return routeId;
	}

	public int getMapId() {
		return mapId;
	}

	public long getTimeOnMap() {
		return System.currentTimeMillis() - spawnTime;
	}
}
