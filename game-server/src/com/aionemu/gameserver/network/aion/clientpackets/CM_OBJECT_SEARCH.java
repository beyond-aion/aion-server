package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnSearchResult;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_NPC_ON_MAP;

/**
 * @author Lyahim
 */
public class CM_OBJECT_SEARCH extends AionClientPacket {

	private int npcId;

	/**
	 * Constructs new client packet instance.
	 * 
	 * @param opcode
	 */
	public CM_OBJECT_SEARCH(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);

	}

	/**
	 * Nothing to do
	 */
	@Override
	protected void readImpl() {
		this.npcId = readD();
	}

	/**
	 * Logging
	 */
	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		if (activePlayer == null) {
			return;
		}
		SpawnSearchResult searchResult = DataManager.SPAWNS_DATA2.getNearestSpawnByNpcId(activePlayer, npcId, activePlayer.getWorldId());
		if (searchResult != null) {
			sendPacket(new SM_SHOW_NPC_ON_MAP(activePlayer, npcId, searchResult.getWorldId(), searchResult.getSpot().getX(), searchResult.getSpot().getY(),
				searchResult.getSpot().getZ()));
		}
	}
}
