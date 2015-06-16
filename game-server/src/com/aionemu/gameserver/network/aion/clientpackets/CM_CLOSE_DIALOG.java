package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HEADING_UPDATE;
import com.aionemu.gameserver.services.DialogService;
import com.aionemu.gameserver.services.player.PlayerMailboxState;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		targetObjectId = readD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		final VisibleObject obj = player.getKnownList().getObject(targetObjectId);
		final AionConnection client = getConnection();
		if(obj == null) {
			return;
		}

		if (obj instanceof Npc) {
			Npc npc = (Npc)obj;
			npc.getAi2().onCreatureEvent(AIEventType.DIALOG_FINISH, player);
			DialogService.onCloseDialog(npc, player);

			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					client.sendPacket(new SM_HEADING_UPDATE(targetObjectId, (byte)obj.getHeading()));
				}
			}, 1200);			
			
			
		}

		if (player.getMailbox().mailBoxState != 0) {
			player.getMailbox().mailBoxState = PlayerMailboxState.CLOSED;
		}
	}
}
