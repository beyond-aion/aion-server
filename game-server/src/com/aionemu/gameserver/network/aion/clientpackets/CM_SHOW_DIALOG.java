package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.TalkInfo;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author alexa026, Avol, ATracer
 */
public class CM_SHOW_DIALOG extends AionClientPacket {

	private int targetObjectId;

	public CM_SHOW_DIALOG(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isProtectionActive())
			player.getController().stopProtectionActiveTask();

		if (player.isTrading())
			return;


		if (player.getKnownList().getObject(targetObjectId) instanceof Npc target) {
			TalkInfo talkInfo = target.getObjectTemplate().getTalkInfo();
			if (talkInfo != null && !talkInfo.isCanTalkInvisible() && player.isInAnyHide()) {
				boolean use = switch (target.getAi().getName()) {
					case "artifact", "useitem" -> true;
					default -> false;
				};
				sendPacket(use ? SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_USE_INVISIBLE_TARGET() : SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_TALK_INVISIBLE_TARGET());
				return;
			}
			target.getController().onDialogRequest(player);
		}
	}
}
