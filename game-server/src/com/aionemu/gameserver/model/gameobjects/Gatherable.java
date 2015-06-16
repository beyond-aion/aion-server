package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.GatherableController;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class Gatherable extends VisibleObject {

	public Gatherable(SpawnTemplate spawnTemplate, VisibleObjectTemplate objectTemplate, int objId,
		GatherableController controller) {
		super(objId, controller, spawnTemplate, objectTemplate, new WorldPosition(spawnTemplate.getWorldId()));
		controller.setOwner(this);
	}

	@Override
	public String getName() {
		return objectTemplate.getName();
	}

	@Override
	public GatherableTemplate getObjectTemplate() {
		return (GatherableTemplate) objectTemplate;
	}

	@Override
	public GatherableController getController() {
		return (GatherableController) super.getController();
	}
}
