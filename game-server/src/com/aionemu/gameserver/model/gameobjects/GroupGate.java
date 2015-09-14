package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author LokiReborn
 */
public class GroupGate extends SummonedObject<Creature> {

	/**
	 * @param objId
	 * @param controller
	 * @param spawnTemplate
	 * @param objectTemplate
	 */
	public GroupGate(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate) {
		super(objId, controller, spawnTemplate, objectTemplate, (byte) 1);
	}

	/**
	 * @return NpcObjectType.GROUPGATE
	 */
	@Override
	public NpcObjectType getNpcObjectType() {
		return NpcObjectType.GROUPGATE;
	}
}
