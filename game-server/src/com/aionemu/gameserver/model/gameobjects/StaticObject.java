package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.StaticObjectController;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class StaticObject extends VisibleObject {

	public StaticObject(StaticObjectController controller, SpawnTemplate spawnTemplate, VisibleObjectTemplate objectTemplate) {
		super(IDFactory.getInstance().nextId(), controller, spawnTemplate, objectTemplate, new WorldPosition(spawnTemplate.getWorldId()), true);
		controller.setOwner(this);
	}
}
