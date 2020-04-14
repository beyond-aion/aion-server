package com.aionemu.gameserver.model.curingzone;

import com.aionemu.gameserver.controllers.VisibleObjectController;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.curingzones.CuringTemplate;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.NpcKnownList;

/**
 * @author xTz
 */
public class CuringObject extends VisibleObject {

	private CuringTemplate template;
	private float range;

	public CuringObject(CuringTemplate template, int instanceId) {
		super(IDFactory.getInstance().nextId(), new VisibleObjectController<CuringObject>() {
		}, null, null, World.getInstance().createPosition(template.getMapId(), template.getX(), template.getY(), template.getZ(), (byte) 0, instanceId), true);
		this.template = template;
		this.range = template.getRange();
		setKnownlist(new NpcKnownList(this));
	}

	public CuringTemplate getTemplate() {
		return template;
	}

	@Override
	public String getName() {
		return "";
	}

	public float getRange() {
		return range;
	}

	public void spawn() {
		World w = World.getInstance();
		w.storeObject(this);
		w.spawn(this);
	}
}
