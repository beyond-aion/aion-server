package com.aionemu.gameserver.world.zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Area;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.handler.AdvancedZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;

/**
 * @author ATracer
 */
public class ZoneInstance implements Comparable<ZoneInstance> {

	private ZoneInfo template;
	private int mapId;
	private Map<Integer, Creature> creatures = new HashMap<>();
	protected List<ZoneHandler> handlers = new ArrayList<>();
	protected List<ZoneHandler> questZoneHandlers = new ArrayList<>();

	public ZoneInstance(int mapId, ZoneInfo template) {
		this.template = template;
		this.mapId = mapId;
	}

	/**
	 * @return the template
	 */
	public Area getAreaTemplate() {
		return template.getArea();
	}

	/**
	 * @return the template
	 */
	public ZoneTemplate getZoneTemplate() {
		return template.getZoneTemplate();
	}

	public boolean revalidate(Creature creature) {
		return (mapId == creature.getWorldId() && template.getArea().isInside3D(creature.getX(), creature.getY(), creature.getZ()));
	}

	public synchronized boolean onEnter(Creature creature) {
		if (creatures.containsKey(creature.getObjectId()))
			return false;
		creatures.put(creature.getObjectId(), creature);
		if (creature instanceof Player)
			creature.getController().onEnterZone(this);
		for (int i = 0; i < handlers.size(); i++)
			handlers.get(i).onEnterZone(creature, this);
		return true;
	}

	public synchronized boolean onLeave(Creature creature) {
		if (!creatures.containsKey(creature.getObjectId()))
			return false;
		creatures.remove(creature.getObjectId());
		creature.getController().onLeaveZone(this);
		for (int i = 0; i < handlers.size(); i++)
			handlers.get(i).onLeaveZone(creature, this);
		return true;
	}

	public boolean onDie(Creature attacker, Creature target) {
		if (!creatures.containsKey(target.getObjectId()))
			return false;
		for (int i = 0; i < handlers.size(); i++) {
			ZoneHandler handler = handlers.get(i);
			if (handler instanceof AdvancedZoneHandler) {
				if (((AdvancedZoneHandler) handler).onDie(attacker, target, this))
					return true;
			}
		}
		return false;
	}

	public boolean isInsideCreature(Creature creature) {
		return creatures.containsKey(creature.getObjectId());
	}

	public boolean isInsideCordinate(float x, float y, float z) {
		return template.getArea().isInside3D(x, y, z);
	}

	@Override
	public int compareTo(ZoneInstance o) {
		int result = getZoneTemplate().getPriority() - o.getZoneTemplate().getPriority();
		if (result == 0) {
			return template.getZoneTemplate().getName().id() - o.template.getZoneTemplate().getName().id();
		}
		return result;
	}

	public void addHandler(ZoneHandler handler) {
		this.handlers.add(handler);
		if (handler instanceof AbstractQuestHandler)
			this.questZoneHandlers.add(handler);
	}

	public boolean hasQuestZoneHandlers() {
		return this.questZoneHandlers.size() != 0;
	}

	public boolean canFly() {
		if (template.getZoneTemplate().getFlags() == -1 || template.getZoneTemplate().getFlags() == 0
			|| World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.FLY))
			return World.getInstance().getWorldMap(mapId).isPossibleFly();
		return (template.getZoneTemplate().getFlags() & ZoneAttributes.FLY.getId()) != 0;
	}

	public boolean canGlide() {
		if (template.getZoneTemplate().getFlags() == -1 || template.getZoneTemplate().getFlags() == 0
			|| World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.GLIDE))
			return World.getInstance().getWorldMap(mapId).canGlide();
		return (template.getZoneTemplate().getFlags() & ZoneAttributes.GLIDE.getId()) != 0;
	}

	public boolean canPutKisk() {
		if (template.getZoneTemplate().getFlags() == -1 || template.getZoneTemplate().getFlags() == 0
			|| World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.BIND))
			return World.getInstance().getWorldMap(mapId).canPutKisk();
		return (template.getZoneTemplate().getFlags() & ZoneAttributes.BIND.getId()) != 0;
	}

	public boolean canRecall() {
		if (template.getZoneTemplate().getFlags() == -1 || template.getZoneTemplate().getFlags() == 0
			|| World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.RECALL)) {
			return World.getInstance().getWorldMap(mapId).canRecall();
		}
		return (template.getZoneTemplate().getFlags() & ZoneAttributes.RECALL.getId()) != 0;
	}

	public boolean canReturnToBattle() {
		return World.getInstance().getWorldMap(mapId).canReturnToBattle();
	}

	public boolean canRide() {
		if (template.getZoneTemplate().getFlags() == -1 || template.getZoneTemplate().getFlags() == 0
			|| World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.RIDE)) {
			return World.getInstance().getWorldMap(mapId).canRide();
		}
		return (template.getZoneTemplate().getFlags() & ZoneAttributes.RIDE.getId()) != 0;
	}

	public boolean canFlyRide() {
		if (template.getZoneTemplate().getFlags() == -1 || template.getZoneTemplate().getFlags() == 0
			|| World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.FLY_RIDE))
			return World.getInstance().getWorldMap(mapId).canFlyRide();
		return (template.getZoneTemplate().getFlags() & ZoneAttributes.FLY_RIDE.getId()) != 0;
	}

	public boolean isPvpAllowed() {
		if (template.getZoneTemplate().getZoneType() != ZoneClassName.PVP)
			return World.getInstance().getWorldMap(mapId).isPvpAllowed();
		return (template.getZoneTemplate().getFlags() & ZoneAttributes.PVP_ENABLED.getId()) != 0;
	}

	public boolean isSameRaceDuelsAllowed() {
		if (template.getZoneTemplate().getZoneType() != ZoneClassName.DUEL || template.getZoneTemplate().getFlags() == 0
			|| World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.DUEL_SAME_RACE_ENABLED))
			return World.getInstance().getWorldMap(mapId).isSameRaceDuelsAllowed();
		return (template.getZoneTemplate().getFlags() & ZoneAttributes.DUEL_SAME_RACE_ENABLED.getId()) != 0;
	}

	public boolean isOtherRaceDuelsAllowed() {
		if (template.getZoneTemplate().getZoneType() != ZoneClassName.DUEL || template.getZoneTemplate().getFlags() == 0
			|| World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.DUEL_OTHER_RACE_ENABLED))
			return World.getInstance().getWorldMap(mapId).isOtherRaceDuelsAllowed();
		return (template.getZoneTemplate().getFlags() & ZoneAttributes.DUEL_OTHER_RACE_ENABLED.getId()) != 0;
	}

	public int getTownId() {
		return template.getZoneTemplate().getTownId();
	}

	/**
	 * @return the creatures
	 */
	public Map<Integer, Creature> getCreatures() {
		return creatures;
	}
	
	public boolean isDominionZone() {
		return template.getZoneTemplate().getZoneType() == ZoneClassName.DOMINION;
	}
}
