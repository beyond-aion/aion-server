package com.aionemu.gameserver.world.navmesh;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.navEngine.NavMeshMap;
import com.aionemu.gameserver.navEngine.NavMeshWorldLoader;
import org.recast4j.detour.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavMeshService {

    private final Logger logger = LoggerFactory.getLogger(NavMeshService.class);

    private final Map<Integer, NavMeshMap> navMeshMaps = new HashMap<>();

    public void initializeNavMeshes() {
        DataManager.WORLD_MAPS_DATA.forEach(map -> navMeshMaps.put(map.getMapId(), new NavMeshMap(map.getMapId())));
        NavMeshWorldLoader.load(navMeshMaps.values());
    }


    public boolean mapSupportsNavMesh(int worldId) {
        return navMeshMaps.containsKey(worldId) && navMeshMaps.get(worldId).getNavMesh() != null;
    }

    public List<StraightPathItem> calculatePath(int worldId, float startX, float startY, float startZ, float endX, float endY, float endZ) {
        NavMeshMap navMeshMap = navMeshMaps.get(worldId);
        if (navMeshMap != null) {
            logger.warn("Found NavMesh.java for " + DataManager.WORLD_MAPS_DATA.getTemplate(worldId).getName());
            NavMeshQuery query = new NavMeshQuery(navMeshMap.getNavMesh());
            QueryFilter filter = new DefaultQueryFilter();
            float[] startPos = { startY, startZ, startX };
            float[] endPos = { endY, endZ, endX };
            float[] polyPickExt = { 2, 4, 2 };
            Result<FindNearestPolyResult> startRefNearestResult = query.findNearestPoly(startPos, polyPickExt, filter);
            Result<FindNearestPolyResult> endRefNearestResult =query.findNearestPoly(endPos, polyPickExt, filter);

            if (startRefNearestResult.succeeded() && endRefNearestResult.succeeded()) {
                logger.warn("Found Start and End Ref: " + startRefNearestResult.result.getNearestRef() + "/" + endRefNearestResult.result.getNearestRef());
                long startRef = startRefNearestResult.result.getNearestRef();
                long endRef = endRefNearestResult.result.getNearestRef();

                List<Long> pathResult = query.findPath(startRef, endRef, startPos, endPos, filter, NavMeshQuery.DT_FINDPATH_ANY_ANGLE, 50).result;
                if (pathResult != null && !pathResult.isEmpty()) {
                    logger.warn("Found Path result: " + pathResult.size());
                    // In case of partial path, make sure the end point is clamped to the last polygon.
                    float[] epos = new float[] { endPos[0], endPos[1], endPos[2] };
                    if (pathResult.get(pathResult.size() - 1) != endRef) {
                        logger.warn("Clamping..");
                        Result<ClosestPointOnPolyResult> result = query
                                .closestPointOnPoly(pathResult.get(pathResult.size() - 1), endPos);
                        if (result.succeeded()) {
                            epos = result.result.getClosest();
                            logger.warn("Clamped successfully");
                        }
                    }
                    List<StraightPathItem> straightPathItems = query.findStraightPath(startPos, epos, pathResult, 50,
                            0).result;
                    logger.warn("Calculated straightPath: " + (straightPathItems == null ? "null" : straightPathItems.size()));
                    return straightPathItems;
                } else {
                    logger.warn("Path Result is null or empty: " + (pathResult == null ? "null" : "empty"));
                }
            } else {
                logger.warn("Start Ref or End Ref are null");
            }
        } else {
            logger.warn("NavMesh.java for map is null " + DataManager.WORLD_MAPS_DATA.getTemplate(worldId).getName());
        }
        return null;
    }

    public List<StraightPathItem> calculatePath(VisibleObject object, float targetX, float targetY, float targetZ) {
        return calculatePath(object.getWorldId(), object.getX(), object.getY(), object.getZ(), targetX, targetY, targetZ);
    }
    public List<StraightPathItem> calculatePath(VisibleObject object, VisibleObject target) {
        return calculatePath(object, target.getX(), target.getY(), target.getZ());
    }

    public float[] getRandomPoint(VisibleObject object, float radius)  {
        NavMeshMap navMeshMap = navMeshMaps.get(object.getWorldId());
        if (navMeshMap != null) {
            NavMeshQuery query = new NavMeshQuery(navMeshMap.getNavMesh());
            QueryFilter filter = new DefaultQueryFilter();
            float[] startPos = { object.getY(), object.getZ(), object.getX() };
            float[] polyPickExt = { 2, 4, 2 };
            Result<FindNearestPolyResult> startRefNearestResult = query.findNearestPoly(startPos, polyPickExt, filter);
            if (startRefNearestResult.succeeded()) {
                Result<FindRandomPointResult> result = query.findRandomPointAroundCircle(startRefNearestResult.result.getNearestRef(), startPos, radius, filter, new NavMeshQuery.FRand(), PolygonByCircleConstraint.strict());
                if (result.succeeded()) {
                    float[] point = result.result.getRandomPt();
                    return new float[]{point[2], point[0], point[1]};
                }
            }
        }
        return null;
    }

    public static NavMeshService getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder {
        protected static final NavMeshService instance = new NavMeshService();
    }
}
