package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RESTORE_CHARACTER;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * In this packets aion client is requesting cancellation of character deleting.
 * 
 * @author -Nemesiss-
 */
public class CM_RESTORE_CHARACTER extends AionClientPacket {

	/**
	 * PlayOk2 - we dont care...
	 */
	@SuppressWarnings("unused")
	private int playOk2;
	/**
	 * ObjectId of character that deletion should be canceled
	 */
	private int chaOid;

	/**
	 * Constructs new instance of <tt>CM_RESTORE_CHARACTER </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_RESTORE_CHARACTER(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		playOk2 = readD();
		chaOid = readD();
	}

	@Override
	protected void runImpl() {
		Account account = getConnection().getAccount();
		PlayerAccountData pad = account.getPlayerAccountData(chaOid);

		boolean success = pad != null && PlayerService.cancelPlayerDeletion(pad);
		sendPacket(new SM_RESTORE_CHARACTER(chaOid, success));
	}
}
