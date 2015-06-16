package com.aionemu.gameserver.world;

import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer
 */
public class WorldMap2DInstance extends WorldMapInstance {

	private int ownerId;

	public WorldMap2DInstance(WorldMap parent, int instanceId, int ownerId) {
		super(parent, instanceId);
		this.ownerId = ownerId;
	}

	@Override
	protected MapRegion createMapRegion(int regionId) {
		float startX = RegionUtil.getXFrom2dRegionId(regionId);
		float startY = RegionUtil.getYFrom2dRegionId(regionId);
		int size = this.getParent().getWorldSize();
		float maxZ = Math.round((float) size / regionSize) * regionSize;
		ZoneInstance[] zones = filterZones(this.getMapId(), regionId, startX, startY, 0, maxZ);
		return new MapRegion(regionId, this, zones);
	}

	protected void initMapRegions() {
		int size = this.getParent().getWorldSize();
		// Create all mapRegion
		for (int x = 0; x <= size; x = x + regionSize) {
			for (int y = 0; y <= size; y = y + regionSize) {
				int regionId = RegionUtil.get2dRegionId(x, y);
				regions.put(regionId, createMapRegion(regionId));
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

	/**
	 * @return the ownerId
	 */
	public int getOwnerId() {
		return ownerId;
	}

	/**
	 * @param ownerId
	 *          the ownerId to set
	 */
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public boolean isPersonal() {
		return ownerId != 0;
	}

}
