package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.GatherableController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author ATracer
 */
public class Gatherable extends VisibleObject {

	public Gatherable(SpawnTemplate spawnTemplate, int objId, GatherableController controller) {
		super(objId, controller, spawnTemplate, DataManager.GATHERABLE_DATA.getGatherableTemplate(spawnTemplate.getNpcId()), new WorldPosition(spawnTemplate.getWorldId()));
		controller.setOwner(this);
		setKnownlist(new PlayerAwareKnownList(this));
	}

	@Override
	public GatherableTemplate getObjectTemplate() {
		return (GatherableTemplate) super.getObjectTemplate();
	}

	@Override
	public GatherableController getController() {
		return (GatherableController) super.getController();
	}

	@Override
	protected boolean autoReleaseObjectId() {
		return true;
	}
}
