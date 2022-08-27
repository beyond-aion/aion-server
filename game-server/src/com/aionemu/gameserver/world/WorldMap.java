package com.aionemu.gameserver.world;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.world.zone.ZoneAttributes;

/**
 * This object is representing one in-game map and can have instances.
 * 
 * @author -Nemesiss-
 */
public class WorldMap implements Iterable<WorldMapInstance> {

	private WorldMapTemplate worldMapTemplate;
	private AtomicInteger nextInstanceId = new AtomicInteger(0);
	private Map<Integer, WorldMapInstance> instances = new ConcurrentHashMap<>();
	private int worldOptions;

	public WorldMap(WorldMapTemplate worldMapTemplate) {
		this.worldMapTemplate = worldMapTemplate;
		this.worldOptions = worldMapTemplate.getFlags();

		for (int i = 1; i <= getInstanceCount(); i++) {
			WorldMapInstanceFactory.createWorldMapInstance(this, 0);
		}
	}

	public String getName() {
		return worldMapTemplate.getName();
	}

	public int getWaterLevel() {
		return worldMapTemplate.getWaterLevel();
	}

	public int getDeathLevel() {
		return worldMapTemplate.getDeathLevel();
	}

	public WorldType getWorldType() {
		return worldMapTemplate.getWorldType();
	}

	public int getWorldSize() {
		return worldMapTemplate.getWorldSize();
	}

	public WorldDropType getWorldDropType() {
		return worldMapTemplate.getWorldDropType();
	}

	public int getMapId() {
		return worldMapTemplate.getMapId();
	}

	public boolean isFlightAllowed() {
		return (worldOptions & ZoneAttributes.FLY.getId()) != 0;
	}

	public boolean isExceptBuff() {
		return worldMapTemplate.isExceptBuff();
	}

	public boolean canGlide() {
		return (worldOptions & ZoneAttributes.GLIDE.getId()) != 0;
	}

	public boolean canPutKisk() {
		return (worldOptions & ZoneAttributes.BIND.getId()) != 0;
	}

	public boolean canRecall() {
		return (worldOptions & ZoneAttributes.RECALL.getId()) != 0;
	}

	public boolean canRide() {
		return (worldOptions & ZoneAttributes.RIDE.getId()) != 0;
	}

	public boolean canFlyRide() {
		return (worldOptions & ZoneAttributes.FLY_RIDE.getId()) != 0;
	}

	public boolean isPvpAllowed() {
		return (worldOptions & ZoneAttributes.PVP_ENABLED.getId()) != 0;
	}

	public boolean isSameRaceDuelsAllowed() {
		return (worldOptions & ZoneAttributes.DUEL_SAME_RACE_ENABLED.getId()) != 0;
	}

	public boolean isOtherRaceDuelsAllowed() {
		return (worldOptions & ZoneAttributes.DUEL_OTHER_RACE_ENABLED.getId()) != 0;
	}

	public boolean canReturnToBattle() {
		return (worldOptions & ZoneAttributes.NO_RETURN_BATTLE.getId()) == 0;
	}

	public void setWorldOption(ZoneAttributes option) {
		worldOptions |= option.getId();
	}

	public void removeWorldOption(ZoneAttributes option) {
		worldOptions &= ~option.getId();
	}

	public boolean hasOverridenOption(ZoneAttributes option) {
		if ((worldMapTemplate.getFlags() & option.getId()) == 0)
			return (worldOptions & option.getId()) != 0;
		return (worldOptions & option.getId()) == 0;
	}

	public int getInstanceCount() {
		int twinCount = worldMapTemplate.getTwinCount();
		if (twinCount == 0)
			twinCount = 1;
		twinCount += worldMapTemplate.getBeginnerTwinCount();
		return twinCount;
	}

	/**
	 * Return a WorldMapInstance - depends on map configuration one map may have twins instances to balance player. This method will return
	 * WorldMapInstance by server chose.
	 * 
	 * @return WorldMapInstance.
	 */
	public WorldMapInstance getMainWorldMapInstance() {
		// TODO Balance players into instances.
		return instances.get(1);
	}

	public WorldMapInstance getWorldMapInstance(int instanceId) {
		// instanceId is a count, some code still uses 0 for the default instance
		if (instanceId == 0)
			instanceId = 1;
		if (!isInstanceType()) {
			if (instanceId > getInstanceCount()) {
				throw new IllegalArgumentException("WorldMapInstance " + getMapId() + " has lower instances count than " + instanceId);
			}
		}
		return instances.get(instanceId);
	}

	/**
	 * Remove WorldMapInstance by instanceId.
	 * 
	 * @param instanceId
	 */
	public void removeWorldMapInstance(int instanceId) {
		// instanceId is a count, some code still uses 0 for the default instance
		if (instanceId == 0)
			instanceId = 1;
		instances.remove(instanceId);
	}

	/**
	 * Add instance to map
	 * 
	 * @param instanceId
	 * @param instance
	 */
	public void addInstance(int instanceId, WorldMapInstance instance) {
		// instanceId is a count, some code still uses 0 for the default instance
		if (instanceId == 0)
			instanceId = 1;
		instances.put(instanceId, instance);
	}

	public final WorldMapTemplate getTemplate() {
		return worldMapTemplate;
	}

	/**
	 * @return the nextInstanceId
	 */
	public int getNextInstanceId() {
		return nextInstanceId.incrementAndGet();
	}

	/**
	 * Whether this world map is instance type
	 * 
	 * @return
	 */
	public boolean isInstanceType() {
		return worldMapTemplate.isInstance();
	}

	public Iterator<WorldMapInstance> iterator() {
		return instances.values().iterator();
	}

	/**
	 * All instance ids of this map
	 */
	public Collection<Integer> getAvailableInstanceIds() {
		return instances.keySet();
	}
}
