package com.aionemu.gameserver.model.shield;

import com.aionemu.gameserver.controllers.ShieldController;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.shield.ShieldTemplate;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.knownlist.SphereKnownList;

/**
 * @author Wakizashi
 */
public class Shield extends VisibleObject {

	private ShieldTemplate template;
	private String name;
	private int id;

	public Shield(ShieldTemplate template) {
		super(IDFactory.getInstance().nextId(), new ShieldController(), null, null, null, true);
		((ShieldController) getController()).setOwner(this);
		this.template = template;
		this.name = (template.getName() == null) ? "SHIELD" : template.getName();
		this.id = template.getId();
		setKnownlist(new SphereKnownList(this, template.getRadius() * 2));
	}

	public ShieldTemplate getTemplate() {
		return template;
	}

	@Override
	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public void spawn() {
		World w = World.getInstance();
		WorldPosition position = w.createPosition(template.getMap(), template.getCenter().getX(), template.getCenter().getY(), template.getCenter()
			.getZ(), (byte) 0, 0);
		this.setPosition(position);
		w.storeObject(this);
		w.spawn(this);
	}
}
