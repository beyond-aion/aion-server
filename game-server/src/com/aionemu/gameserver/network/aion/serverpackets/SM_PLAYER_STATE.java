package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
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
	private boolean redrawSurroundings;

	public SM_PLAYER_STATE(Creature creature) {
		this(creature, false);
	}

	/**
	 * @param redrawSurroundings
	 *          if the packet is about the receiving player, the client redraws all objects around it according to its new see state
	 */
	public SM_PLAYER_STATE(Creature creature, boolean redrawSurroundings) {
		this.playerObjId = creature.getObjectId();
		this.visualState = creature.getVisualState();
		this.seeState = creature.getSeeState();
		this.redrawSurroundings = redrawSurroundings;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeC(visualState);
		writeC(seeState);
		writeC(redrawSurroundings ? 1 : 0);
	}
}
