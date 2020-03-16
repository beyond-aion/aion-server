package com.aionemu.gameserver.model.templates.zone;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Spatial;

/**
 * @author Rolandas, Neon
 */
public class MaterialZoneTemplate extends ZoneTemplate {

	public MaterialZoneTemplate(Spatial geometry, int mapId) {
		mapid = mapId;
		flags = DataManager.WORLD_MAPS_DATA.getTemplate(mapId).getFlags();
		setXmlName(geometry.getName() + "_" + mapId);
		BoundingBox box = (BoundingBox) geometry.getWorldBound();
		Vector3f center = box.getCenter();
		// don't use polygons for small areas, they are bugged in Java API
		if (geometry.getName().contains("CYLINDER") || geometry.getName().contains("CONE") || geometry.getName().contains("H_COLUME")) {
			areaType = AreaType.CYLINDER;
			float r = (float) Math.sqrt(box.getXExtent() * box.getXExtent() + box.getYExtent() * box.getYExtent());
			cylinder = new Cylinder(center.x, center.y, r + 1, center.z + box.getZExtent() + 1, center.z - box.getZExtent() - 1);
		} else if (geometry.getName().contains("SEMISPHERE")) {
			areaType = AreaType.SEMISPHERE;
			semisphere = new Semisphere(center.x, center.y, center.z, calculateDistanceFromCenterToCorner(box) + 1);
		} else {
			areaType = AreaType.SPHERE;
			sphere = new Sphere(center.x, center.y, center.z, calculateDistanceFromCenterToCorner(box) + 1);
		}
	}

	private float calculateDistanceFromCenterToCorner(BoundingBox box) {
		// all corners are the same distance from the center of the box
		float distanceFromCenterToEdgeSquared = box.getXExtent() * box.getXExtent() + box.getYExtent() * box.getYExtent();
		float distanceFromCenterToConerSquared = distanceFromCenterToEdgeSquared + box.getZExtent() * box.getZExtent();
		return (float) Math.sqrt(distanceFromCenterToConerSquared);
	}
}
