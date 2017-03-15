package com.aionemu.gameserver.model.assemblednpc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xTz
 */
public class AssembledNpc {

	private List<AssembledNpcPart> assembledPatrs = new ArrayList<>();
	private long spawnTime = System.currentTimeMillis();
	private int routeId;
	private int mapId;

	public AssembledNpc(int routeId, int mapId, int liveTime, List<AssembledNpcPart> assembledPatrs) {
		this.assembledPatrs = assembledPatrs;
		this.routeId = routeId;
		this.mapId = mapId;
	}

	public List<AssembledNpcPart> getAssembledParts() {
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
