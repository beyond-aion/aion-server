package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author LokiReborn
 */
public class GroupGate extends SummonedObject<Creature> {

	public GroupGate(NpcController controller, SpawnTemplate spawnTemplate, Creature creator) {
		super(controller, spawnTemplate, (byte) 1, creator);
		setKnownlist(new PlayerAwareKnownList(this));
		setEffectController(new EffectController(this));
	}

	/**
	 * @return NpcObjectType.GROUPGATE
	 */
	@Override
	public NpcObjectType getNpcObjectType() {
		return NpcObjectType.GROUPGATE;
	}
}
