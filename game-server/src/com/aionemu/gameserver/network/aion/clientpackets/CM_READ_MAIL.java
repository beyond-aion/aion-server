package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.mail.MailService;

/**
 * @author kosyachok
 */
public class CM_READ_MAIL extends AionClientPacket {

	int mailObjId;

	public CM_READ_MAIL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		mailObjId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		MailService.getInstance().readMail(player, mailObjId);
	}
}
