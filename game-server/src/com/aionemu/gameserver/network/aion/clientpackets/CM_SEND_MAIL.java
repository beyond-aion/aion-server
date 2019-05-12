package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.mail.MailService;

/**
 * @author Aion Gates, xTz
 */
public class CM_SEND_MAIL extends AionClientPacket {

	private String recipientName;
	private String title;
	private String message;
	private int itemObjId;
	private long itemCount;
	private long kinahCount;
	private int idLetterType;

	public CM_SEND_MAIL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		recipientName = readS();
		title = readS();
		message = readS();
		itemObjId = readD();
		itemCount = readQ();
		kinahCount = readQ();
		idLetterType = readUC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		MailService.sendMail(player, recipientName, title, message, itemObjId, itemCount, kinahCount, LetterType.getLetterTypeById(idLetterType));
	}
}
