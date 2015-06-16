package com.aionemu.gameserver.ai2;

import com.aionemu.gameserver.controllers.SummonController;
import com.aionemu.gameserver.controllers.movement.SummonMoveController;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 *
 * @author xTz
 */
@AIName("summon")
public class AISummon extends AITemplate {

	@Override
	public Summon getOwner() {
		return (Summon) super.getOwner();
	}

	protected NpcTemplate getObjectTemplate() {
		return getOwner().getObjectTemplate();
	}

	protected SpawnTemplate getSpawnTemplate() {
		return getOwner().getSpawn();
	}

	protected Race getRace() {
		return getOwner().getRace();
	}

	protected Player getMaster() {
		return getOwner().getMaster();
	}

	protected SummonMoveController getMoveController() {
		return getOwner().getMoveController();
	}

	protected SummonController getController() {
		return getOwner().getController();
	}
}
