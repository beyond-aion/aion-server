package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_UPDATE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * Client Sends this packet when /Select NAME is typed.<br>
 * I believe it's the same as mouse click on a character.<br>
 * If client want's to select target - d is object id.<br>
 * If client unselects target - d is 0;
 *
 * @author SoulKeeper, Sweetkr, KID
 */
public class CM_TARGET_SELECT extends AionClientPacket {

	/**
	 * Target object id that client wants to select or 0 if wants to unselect
	 */
	private int targetObjectId;
	private byte type;

	/**
	 * Constructs new client packet instance.
	 *
	 * @param opcode
	 */
	public CM_TARGET_SELECT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	/**
	 * Read packet.<br>
	 * d - object id; c - selection type;
	 */
	@Override
	protected void readImpl() {
		targetObjectId = readD();
		type = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		VisibleObject obj;
		VisibleObject oldTarget = player.getTarget();

		if (targetObjectId == player.getObjectId())
			obj = player;
		else {
			obj = player.getKnownList().getObject(targetObjectId);

			if (obj == null && player.isInTeam()) {
				TeamMember<Player> member = player.getCurrentTeam().getMember(targetObjectId);
				if (member != null) {
					obj = member.getObject();
				}
			}
		}

		if (obj != null) {
			if (type == 1) {
				if (obj.getTarget() == null)
					return;
				player.setTarget(obj.getTarget());
			} else
				player.setTarget(obj);

			if (!player.equals(obj) && !player.canSee(obj))
				AuditLogger.log(player, "possibly used radar hack: targeting invisible " + obj);
		} else
			player.setTarget(null);

		if (oldTarget instanceof Npc) {
			Npc npc = (Npc) oldTarget;
			if (npc.getObjectTemplate().isDialogNpc()) {
				npc.getAi().think();
			}
		}

		sendPacket(new SM_TARGET_SELECTED(player.getTarget()));
		PacketSendUtility.broadcastToSightedPlayers(player, new SM_TARGET_UPDATE(player));
	}
}
