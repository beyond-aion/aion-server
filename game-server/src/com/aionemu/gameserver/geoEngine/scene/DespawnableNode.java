package com.aionemu.gameserver.geoEngine.scene;

import java.util.BitSet;

import com.aionemu.gameserver.geoEngine.collision.Collidable;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.event.EventService;

public class DespawnableNode extends Node {

	public DespawnableType type = DespawnableType.NONE;
	public int id = 0; //
	public byte levelBitMask = 0;
	private final BitSet instances = new BitSet();

	public void setActive(int instanceId, boolean active) {
		synchronized (instances) {
			instances.set(instanceId, active);
		}
	}

	public boolean isActive(int instanceId) {
		synchronized (instances) {
			return instances.get(instanceId);
		}
	}

	public void copyFrom(Node node) throws CloneNotSupportedException {
		this.name = node.name;
		this.collisionIntentions = node.collisionIntentions;
		this.materialId = node.materialId;
		for (Spatial spatial : node.getChildren()) {
			if (spatial instanceof Geometry) {
				Geometry geom = new Geometry(spatial.getName(), ((Geometry) spatial).getMesh());
				attachChild(geom);
			} else if (spatial instanceof Node) {
				attachChild(((Node) (spatial)).clone());
			} else {
				throw new CloneNotSupportedException();
			}
		}
	}

	@Override
	public int collideWith(Collidable other, CollisionResults results) {
		if (type == DespawnableType.EVENT) {
			if (EventService.getInstance().getEventTheme().getId() != id)
				return 0;
		} else {
			if (type != DespawnableType.HOUSE && !isActive(results.getInstanceId())) {
				return 0;
			} else if ((results.getIgnoreProperties() != null)) {
				if (type == DespawnableType.SHIELD
					&& (results.getIgnoreProperties().getRace() != null || results.getIgnoreProperties() == IgnoreProperties.ANY_RACE)) {
					SiegeLocation loc = SiegeService.getInstance().getSiegeLocation(id);
					if (loc != null) {
						if (results.getIgnoreProperties() == IgnoreProperties.ANY_RACE
							|| loc.getRace() != SiegeRace.BALAUR && results.getIgnoreProperties().getRace().getRaceId() == loc.getRace().getRaceId()
							|| loc.getRace() == SiegeRace.BALAUR && results.getIgnoreProperties() == IgnoreProperties.BALAUR) {
							return 0;
						}
					}
				}
				if (results.getIgnoreProperties().getStaticId() > 0 && results.getIgnoreProperties().getStaticId() == id) {
					return 0;
				}
			}
		}
		return super.collideWith(other, results);
	}

	@Override
	public Node clone() throws CloneNotSupportedException {
		DespawnableNode node = new DespawnableNode();
		node.type = type;
		node.id = id;
		node.levelBitMask = levelBitMask;
		node.instances.or(instances);
		node.name = name;
		node.collisionIntentions = collisionIntentions;
		node.materialId = materialId;
		for (Spatial spatial : getChildren()) {
			if (spatial instanceof Geometry) {
				Geometry geom = new Geometry(spatial.getName(), ((Geometry) spatial).getMesh());
				node.attachChild(geom);
			} else if (spatial instanceof Node) {
				node.attachChild(((Node) (spatial)).clone());
			} else {
				throw new CloneNotSupportedException();
			}
		}
		return node;
	}

	public void setType(DespawnableType type) {
		this.type = type;
	}

	public void setId(int id) {
		this.id = id;
	}

	public enum DespawnableType {

		NONE(0),
		EVENT(1),
		PLACEABLE(2),
		HOUSE(3),
		HOUSE_DOOR(4),
		TOWN_OBJECT(5),
		DOOR_STATE1(6),
		DOOR_STATE2(7),
		SHIELD(8);

		private final byte id;

		DespawnableType(int id) {
			this.id = (byte) id;
		}

		public byte getId() {
			return this.id;
		}

		public static DespawnableType getById(byte id) {
			for (DespawnableType type : values()) {
				if (id == type.id)
					return type;
			}
			throw new IllegalArgumentException("Invalid ID " + id);
		}
	}
}
