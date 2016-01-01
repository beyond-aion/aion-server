package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HEADING_UPDATE;
import com.aionemu.gameserver.services.DialogService;
import com.aionemu.gameserver.services.player.PlayerMailboxState;

/**
 * @modified Neon
 */
public class CM_CLOSE_DIALOG extends AionClientPacket {

	/**
	 * Target object id that client wants to TALK WITH or 0 if wants to unselect
	 */
	private int targetObjectId;

	/**
	 * Constructs new instance of <tt>CM_CM_REQUEST_DIALOG </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_CLOSE_DIALOG(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		VisibleObject target = player.getKnownList().getObject(targetObjectId);

		if (target == null)
			return;

		AionConnection client = getConnection();
		Mailbox mailbox = player.getMailbox();

		if (target instanceof Npc) {
			Npc npc = (Npc) target;
			npc.getAi2().onCreatureEvent(AIEventType.DIALOG_FINISH, player);
			DialogService.onCloseDialog(npc, player);

			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					client.sendPacket(new SM_HEADING_UPDATE(targetObjectId, target.getHeading()));
				}
			}, 1200);

		}

		if (mailbox != null && mailbox.mailBoxState != PlayerMailboxState.CLOSED) {
			mailbox.mailBoxState = PlayerMailboxState.CLOSED;
		}
	}
}
