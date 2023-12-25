package com.aionemu.gameserver.navEngine;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.io.MeshSetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

public class NavMeshWorldLoader {

    private static final Logger log = LoggerFactory.getLogger(NavMeshWorldLoader.class);
    public static final  String NAVMESH_DIR = "data/navmesh/";

    public static void load(Collection<NavMeshMap> maps) {
        log.info("loading navmeshes");
        maps.parallelStream().forEach(map -> {
            try {
                loadNavMesh(map);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void loadNavMesh(NavMeshMap map) throws IOException {
        File navmeshFile = new File(NAVMESH_DIR + map.getMapId() + ".navmesh");
        if (!navmeshFile.exists()) {
            if (DataManager.WORLD_MAPS_DATA.getTemplate(map.getMapId()).getWorldSize() != 0) {
                log.warn(navmeshFile + " is missing");
            }
            return;
        }
        NavMesh mesh;
        MeshSetReader reader = new MeshSetReader();
        try (FileInputStream fis = new FileInputStream(navmeshFile)) {
            mesh = reader.read(fis, 6);
        }
        map.setNavMesh(mesh);
    }
}
