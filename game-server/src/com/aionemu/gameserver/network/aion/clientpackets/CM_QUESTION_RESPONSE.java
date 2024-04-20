package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.ExchangeService;

/**
 * Response to SM_QUESTION_WINDOW
 * 
 * @author Ben, Sarynth, Neon
 */
public class CM_QUESTION_RESPONSE extends AionClientPacket {

	private int questionid;
	private int response;
	@SuppressWarnings("unused")
	private int senderid;

	public CM_QUESTION_RESPONSE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		questionid = readD();

		response = readUC(); // y/n
		readC(); // unk 0x00 - 0x01 ?
		readH();
		senderid = readD();
		readD();
		readH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isTrading() && response != 0) // answered request with yes during exchange
			ExchangeService.getInstance().cancelExchange(player);
		player.getResponseRequester().respond(questionid, response);
	}

}
