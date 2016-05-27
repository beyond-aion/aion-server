package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * So far I've found only one usage for this packet - to stop character blinking (just after login into game, player's character is blinking)<br>
 * states: 0 - normal char, 1- crouched invisible char, 64 - standing blinking char 128 - char is invisible
 *
 * @author Luno, Sweetkr
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

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeC(visualState);
		writeC(seeState);
		writeC(visualState == CreatureVisualState.BLINKING.getId() ? 0x01 : 0x00);
	}
}
