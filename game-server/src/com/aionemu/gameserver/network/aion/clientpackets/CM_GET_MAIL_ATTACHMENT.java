package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.mail.MailService;

/**
 * @author kosyachok
 */
public class CM_GET_MAIL_ATTACHMENT extends AionClientPacket {

	private int mailObjId;
	private int attachmentType;

	public CM_GET_MAIL_ATTACHMENT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		mailObjId = readD();
		attachmentType = readC(); // 0 - item , 1 - kinah
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		MailService.getInstance().getAttachments(player, mailObjId, attachmentType);
	}
}
