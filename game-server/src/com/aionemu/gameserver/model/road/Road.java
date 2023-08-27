package com.aionemu.gameserver.model.road;

import com.aionemu.gameserver.controllers.RoadController;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.geometry.Plane3D;
import com.aionemu.gameserver.model.templates.road.RoadTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.SphereKnownList;

/**
 * This class handles roads (teleport ways) between maps (e.g. road from Verteron to Eltnen)
 * Each road is a one-way portal, so for a map-connection in both directions you need two roads.
 * 
 * @author SheppeR
 */
public class Road extends VisibleObject {

	private final RoadTemplate template;
	private final Plane3D plane;

	public Road(RoadTemplate template, Integer instanceId) {
		super(IDFactory.getInstance().nextId(), new RoadController(), null, null, World.getInstance().createPosition(template.getMap(),
			template.getCenter().getX(), template.getCenter().getY(), template.getCenter().getZ(), (byte) 0, instanceId), true);

		((RoadController) getController()).setOwner(this);
		this.template = template;
		Vector3f p1 = new Vector3f(template.getCenter().getX(), template.getCenter().getY(), template.getCenter().getZ());
		Vector3f p2 = new Vector3f(template.getP1().getX(), template.getP1().getY(), template.getP1().getZ());
		Vector3f p3 = new Vector3f(template.getP2().getX(), template.getP2().getY(), template.getP2().getZ());
		this.plane = new Plane3D(p1, p2, p3);
		setKnownlist(new SphereKnownList(this, template.getRadius() * 2));
	}

	public boolean isCrossed(Vector3f oldPosition, Vector3f newPosition) {
		Vector3f intersection = plane.intersection(oldPosition, newPosition);
		return intersection != null && PositionUtil.isInRange(this, intersection.x, intersection.y, intersection.z, template.getRadius());
	}

	public RoadTemplate getTemplate() {
		return template;
	}

	@Override
	public String getName() {
		return template.getName();
	}

	public void spawn() {
		World w = World.getInstance();
		w.storeObject(this);
		w.spawn(this);
	}
}
