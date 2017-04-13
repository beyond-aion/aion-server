package com.aionemu.gameserver.model.road;

import com.aionemu.gameserver.controllers.RoadController;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.road.RoadTemplate;
import com.aionemu.gameserver.model.utils3d.Plane3D;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.SphereKnownList;

/**
 * This class handles roads (teleport ways) between maps (e.g. road from verteron to eltnen)
 * Each road represents is a one-way portal, so for a map-connection in both directions you need two roads.
 * 
 * @author SheppeR
 */
public class Road extends VisibleObject {

	private final RoadTemplate template;
	private final Plane3D plane;
	private final Point3D center;
	private final Point3D p1;
	private final Point3D p2;

	public Road(RoadTemplate template, Integer instanceId) {
		super(IDFactory.getInstance().nextId(), new RoadController(), null, null, World.getInstance().createPosition(template.getMap(),
			template.getCenter().getX(), template.getCenter().getY(), template.getCenter().getZ(), (byte) 0, instanceId));

		((RoadController) getController()).setOwner(this);
		this.template = template;
		this.center = new Point3D(template.getCenter().getX(), template.getCenter().getY(), template.getCenter().getZ());
		this.p1 = new Point3D(template.getP1().getX(), template.getP1().getY(), template.getP1().getZ());
		this.p2 = new Point3D(template.getP2().getX(), template.getP2().getY(), template.getP2().getZ());
		this.plane = new Plane3D(center, p1, p2);
		setKnownlist(new SphereKnownList(this, template.getRadius() * 2));
	}

	public Plane3D getPlane() {
		return plane;
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
