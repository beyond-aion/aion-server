package com.aionemu.gameserver.geoEngine.scene;

import com.aionemu.gameserver.geoEngine.collision.Collidable;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.event.EventService;

import java.util.BitSet;

public class DespawnableNode extends Node {

	public DespawnableType type = DespawnableType.NONE;
	public int id = 0; //
	public byte level = 0;
	private BitSet instances = new BitSet();

	public void setActive(int instanceId, boolean active) {
		instances.set(instanceId, active);
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
			if (EventService.getInstance().getEventTheme().getId() == id) {
				return super.collideWith(other, results);
			} else {
				return 0;
			}
		} else {
			if (!instances.get(results.getInstanceId())) {
				return 0;
			} else if ((results.getIgnoreProperties() != null)) {
				if (results.getIgnoreProperties().getRace() != null && type == DespawnableType.SHIELD) {
					SiegeLocation loc = SiegeService.getInstance().getSiegeLocation(id);
					if (loc != null) {
						if ((results.getIgnoreProperties().getRace() == Race.ANY) || (results.getIgnoreProperties().getRace().getRaceId() == loc.getRace().getRaceId()) ||
								(results.getIgnoreProperties().getRace() == Race.DRAKAN && loc.getRace() == SiegeRace.BALAUR)) {
							return 0;
						}
					}
				}
				if (results.getIgnoreProperties().getStaticId() > 0 && results.getIgnoreProperties().getStaticId() == id) {
					return 0;
				}
			}
			return super.collideWith(other, results);
		}
	}

	@Override
	public Node clone() throws CloneNotSupportedException {
		DespawnableNode node = new DespawnableNode();
		node.type = type;
		node.id = id;
		node.level = level;
		node.instances = (BitSet) instances.clone();
		node.name = name;
		node.collisionIntentions = collisionIntentions;
		node.materialId = materialId;
		for (Spatial spatial : this.getChildren()) {
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
		//2 placeable, 3 house, 4 house-door, 5 town object, 6 door state1, 7 door state2, 8 shield

		public byte id;

		private DespawnableType(int id) { this.id = (byte) id; }

		public byte getId() {
			return this.id;
		}

		public static DespawnableType getTypeWithId(byte id) {
			switch (id) {
				case 1:
					return DespawnableType.EVENT;
				case 2:
					return DespawnableType.PLACEABLE;
				case 3:
					return DespawnableType.HOUSE;
				case 4:
					return DespawnableType.HOUSE_DOOR;
				case 5:
					return DespawnableType.TOWN_OBJECT;
				case 6:
					return DespawnableType.DOOR_STATE1;
				case 7:
					return DespawnableType.DOOR_STATE2;
				case 8:
					return DespawnableType.SHIELD;
				default:
					return DespawnableType.NONE;
			}
		}
	}
}
