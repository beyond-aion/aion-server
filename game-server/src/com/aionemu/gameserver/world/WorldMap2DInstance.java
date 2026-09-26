package com.aionemu.gameserver.world;

import java.util.List;
import java.util.function.Function;

import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.world.zone.RegionZone;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneService;

/**
 * @author ATracer
 */
public class WorldMap2DInstance extends WorldMapInstance {

	private final int ownerId;

	public WorldMap2DInstance(WorldMap parent, int instanceId, int ownerId, int maxPlayers, Function<WorldMapInstance, InstanceHandler> instanceHandlerSupplier) {
		super(parent, instanceId, maxPlayers, instanceHandlerSupplier);
		this.ownerId = ownerId;
	}

	private MapRegion createMapRegion(int regionId, List<ZoneInstance> zoneInstances) {
		float startX = RegionUtil.getXFrom2dRegionId(regionId);
		float startY = RegionUtil.getYFrom2dRegionId(regionId);
		int size = this.getParent().getWorldSize();
		float maxZ = Math.round((float) size / regionSize) * regionSize;
		return createMapRegion(regionId, zoneInstances, new RegionZone(startX, startY, 0, maxZ));
	}

	@Override
	protected void initMapRegions() {
		List<ZoneInstance> zoneInstances = ZoneService.getInstance().createZoneInstances(getMapId());
		int size = this.getParent().getWorldSize();
		// Create all mapRegion
		for (int x = 0; x <= size; x = x + regionSize) {
			for (int y = 0; y <= size; y = y + regionSize) {
				int regionId = RegionUtil.get2dRegionId(x, y);
				regions.put(regionId, createMapRegion(regionId, zoneInstances));
			}
		}

		// Add Neighbour
		for (int x = 0; x <= size; x = x + regionSize) {
			for (int y = 0; y <= size; y = y + regionSize) {
				int regionId = RegionUtil.get2dRegionId(x, y);
				MapRegion mapRegion = regions.get(regionId);
				for (int x2 = x - regionSize; x2 <= x + regionSize; x2 += regionSize) {
					for (int y2 = y - regionSize; y2 <= y + regionSize; y2 += regionSize) {
						if (x2 == x && y2 == y)
							continue;
						int neighbourId = RegionUtil.get2dRegionId(x2, y2);
						MapRegion neighbour = regions.get(neighbourId);
						if (neighbour != null)
							mapRegion.addNeighbourRegion(neighbour);
					}
				}
			}
		}
	}

	@Override
	public MapRegion getRegion(float x, float y, float z) {
		int regionId = RegionUtil.get2dRegionId(x, y);
		return regions.get(regionId);
	}

	@Override
	public int getOwnerId() {
		return ownerId;
	}

	@Override
	public boolean isPersonal() {
		return ownerId != 0;
	}

}
