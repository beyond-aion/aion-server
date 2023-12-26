package com.aionemu.gameserver.navEngine;

import org.recast4j.detour.NavMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class NavMeshMap {

    private static final Logger log = LoggerFactory.getLogger(NavMeshMap.class);

    private final Map<Integer, Long> doorRefs = new HashMap<>();
    private final int mapId;
    private NavMesh navMesh;

    public NavMeshMap(int mapId) {
        this.mapId = mapId;
    }

    public int getMapId() {
        return mapId;
    }

    public NavMesh getNavMesh() {
        return navMesh;
    }

    public void setNavMesh(NavMesh navMesh) {
        this.navMesh = navMesh;
    }

    public Long getDoorRef(int staticId) {
        return doorRefs.getOrDefault(staticId, null);
    }

    public void setDoorRef(int staticId, long ref) {
        doorRefs.put(staticId, ref);
    }
}
