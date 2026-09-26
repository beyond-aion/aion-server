package com.aionemu.gameserver.world.zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.SummonedObject;
import com.aionemu.gameserver.model.geometry.Area;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.utils.collections.CollectionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.handler.AdvancedZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;

/**
 * @author ATracer
 */
public class ZoneInstance {

	private final ZoneInfo template;
	private final int mapId;
	private final Map<Integer, Creature> creatures = new HashMap<>();
	private final List<ZoneHandler> handlers;
	private boolean ignoreRegularNpcs = true;

	public ZoneInstance(int mapId, ZoneInfo template) {
		this(mapId, template, new ArrayList<>(0));
	}

	public ZoneInstance(int mapId, ZoneInfo template, List<ZoneHandler> handlers) {
		this.template = template;
		this.mapId = mapId;
		this.handlers = handlers;
	}

	public Area getAreaTemplate() {
		return template.getArea();
	}

	public ZoneTemplate getZoneTemplate() {
		return template.getZoneTemplate();
	}

	public boolean matches(String zoneName) {
		return template.matches(zoneName);
	}

	public boolean revalidate(Creature creature) {
		return (mapId == creature.getWorldId() && template.getArea().isInside3D(creature.getX(), creature.getY(), creature.getZ()));
	}

	/**
	 * @return true if this zone is irrelevant to the creature, meaning that it should not be tracked by it
	 */
	public boolean isIgnored(Creature creature) {
		return ignoreRegularNpcs && creature instanceof Npc npc && !(npc instanceof SummonedObject<?>);
	}

	public synchronized boolean onEnter(Creature creature) {
		if (creatures.putIfAbsent(creature.getObjectId(), creature) != null)
			return false;
		creature.getController().onEnterZone(this);
		for (ZoneHandler handler : handlers)
			handler.onEnterZone(creature, this);
		return true;
	}

	public synchronized boolean onLeave(Creature creature) {
		if (creatures.remove(creature.getObjectId()) == null)
			return false;
		creature.getController().onLeaveZone(this);
		for (ZoneHandler handler : handlers)
			handler.onLeaveZone(creature, this);
		return true;
	}

	public boolean onDie(Creature attacker, Creature target) {
		if (!creatures.containsKey(target.getObjectId()))
			return false;
		for (ZoneHandler handler : handlers) {
			if (handler instanceof AdvancedZoneHandler advancedZoneHandler) {
				if (advancedZoneHandler.onDie(attacker, target, this))
					return true;
			}
		}
		return false;
	}

	public boolean isInsideCreature(Creature creature) {
		return creatures.containsKey(creature.getObjectId());
	}

	public boolean isInsideCoordinate(float x, float y, float z) {
		return template.getArea().isInside3D(x, y, z);
	}

	public void addHandler(ZoneHandler handler) {
		if (handler.handlesAllCreatures())
			ignoreRegularNpcs = false;
		handlers.add(handler);
	}

	public int getTownId() {
		return template.getZoneTemplate().getTownId();
	}

	public void forEach(Consumer<Creature> action) {
		CollectionUtil.forEach(creatures.values(), action);
	}

	public boolean isDominionZone() {
		return template.getZoneTemplate().getZoneType() == ZoneClassName.DOMINION;
	}
}
