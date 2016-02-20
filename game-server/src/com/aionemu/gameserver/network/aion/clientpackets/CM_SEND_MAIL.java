package com.aionemu.gameserver.network.aion.clientpackets;

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

	public CM_SEND_MAIL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		recipientName = readS();
		title = readS();
		message = readS();
		itemObjId = readD();
		itemCount = readQ();
		kinahCount = readQ();
		idLetterType = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!player.isTrading() && kinahCount < 100000000000L && kinahCount > -1 && itemCount > -2) {
			MailService.getInstance().sendMail(player, recipientName, title, message, itemObjId, itemCount, kinahCount,
				LetterType.getLetterTypeById(idLetterType));
		}
	}
}
