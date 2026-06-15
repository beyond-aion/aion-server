package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * The game client sends this packet when selecting a target via click, hotkey or the chat commands /select name and /selecttargetoftarget.
 *
 * @author SoulKeeper, Sweetkr, KID
 */
public class CM_TARGET_SELECT extends AionClientPacket {

	/**
	 * Target object id that client wants to select or 0 if wants to unselect
	 */
	private int targetObjectId;
	private boolean selectTargetOfTarget;

	public CM_TARGET_SELECT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
		selectTargetOfTarget = readC() == 1;
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		VisibleObject newTarget;
		if (selectTargetOfTarget) {
			if (player.getTarget() == null) {
				sendPacket(SM_SYSTEM_MESSAGE.STR_ASSISTKEY_THIS_IS_ASSISTKEY());
				return;
			}
			newTarget = player.getTarget().getTarget();
			if (newTarget == null) {
				sendPacket(SM_SYSTEM_MESSAGE.STR_ASSISTKEY_NO_USER());
				return;
			}
			if (!newTarget.equals(player) && !player.getKnownList().sees(newTarget)) {
				sendPacket(player.getKnownList().knows(newTarget) ? SM_SYSTEM_MESSAGE.STR_ASSISTKEY_NO_USER() : SM_SYSTEM_MESSAGE.STR_ASSISTKEY_TOO_FAR());
				return;
			}
		} else if (targetObjectId == 0) {
			newTarget = null;
		} else if (targetObjectId == player.getObjectId()) {
			newTarget = player;
		} else {
			newTarget = player.getKnownList().getObject(targetObjectId);
			if (newTarget == null && player.isInTeam() && player.getCurrentTeam().hasMember(targetObjectId))
				newTarget = player.getCurrentTeam().getMember(targetObjectId).getObject();
			else if (newTarget != null && !player.equals(newTarget) && !player.getKnownList().sees(newTarget)) {
				AuditLogger.log(player, "possibly used radar hack: trying to target invisible " + newTarget);
				newTarget = null;
			}
		}
		player.setTarget(newTarget);
	}
}
