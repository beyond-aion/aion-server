package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.ArmsfusionService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author zdead, Wakizashi, Neon
 */
public class CM_FUSION_WEAPONS extends AionClientPacket {

	public CM_FUSION_WEAPONS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	private int npcObjId;
	private int mainWeaponObjId;
	private int fuseWeaponObjId;

	@Override
	protected void readImpl() {
		npcObjId = readD();
		mainWeaponObjId = readD();
		fuseWeaponObjId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isTargetingNpcWithFunction(npcObjId, DialogAction.COMPOUND_WEAPON))
			ArmsfusionService.fusionWeapons(getConnection().getActivePlayer(), mainWeaponObjId, fuseWeaponObjId);
		else
			AuditLogger.log(player, "tried to fuse weapons without targeting an armsfusion officer");
	}
}
