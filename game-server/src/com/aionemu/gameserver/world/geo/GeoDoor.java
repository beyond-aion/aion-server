package com.aionemu.gameserver.world.geo;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.scene.mesh.DoorGeometry;

/**
 * @author Neon
 */
public interface GeoDoor {

	DoorGeometry getGeometry();

	default void setDoorState(int instanceId, boolean open) {
		if (GeoDataConfig.GEO_DOORS_ENABLE && getGeometry() != null)
			getGeometry().setDoorState(instanceId, open);
	}
}
