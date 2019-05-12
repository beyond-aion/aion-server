package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.mail.MailService;

/**
 * @author kosyachok
 */
public class CM_DELETE_MAIL extends AionClientPacket {

	private int[] mailObjIds;

	public CM_DELETE_MAIL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		mailObjIds = new int[readUH()];
		for (int i = 0; i < mailObjIds.length; i++) {
			mailObjIds[i] = readD();
			readC(); // unk
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		MailService.deleteMail(player, mailObjIds);
	}
}
