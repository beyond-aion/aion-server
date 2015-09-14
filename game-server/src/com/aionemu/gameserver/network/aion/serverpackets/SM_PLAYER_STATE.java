package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * So far I've found only one usage for this packet - to stop character blinking ( just after login into game, player's character is blinking )
 *
 * @author Luno, Sweetkr states: 0 - normal char 1- crounched invisible char 64 - standing blinking char 128- char is invisible
 */
public class SM_PLAYER_STATE extends AionServerPacket {

	private int playerObjId;
	private int visualState;
	private int seeState;

	public SM_PLAYER_STATE(Creature creature) {
		this.playerObjId = creature.getObjectId();
		this.visualState = creature.getVisualState();
		this.seeState = creature.getSeeState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeC(visualState);
		writeC(seeState);
		writeC(visualState == 64 ? 0x01 : 0x00);
	}

}
