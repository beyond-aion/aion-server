package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Sent to fill the search panel of a players social window<br />
 * I.E.: In response to a <tt>CM_PLAYER_SEARCH</tt>
 * 
 * @author Ben
 */
public class SM_PLAYER_SEARCH extends AionServerPacket {

	private List<Player> players;
	private int region;

	/**
	 * Constructs a new packet that will send these players
	 * 
	 * @param players
	 *          List of players to show
	 * @param region
	 *          of search - should be passed as parameter to prevent null in player.getActiveRegion()
	 */
	public SM_PLAYER_SEARCH(List<Player> players, int region) {
		this.players = players;
		this.region = region;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(players.size());
		for (Player player : players) {
			writeD(player.getActiveRegion() == null ? region : player.getActiveRegion().getMapId());
			writeF(player.getPosition().getX());
			writeF(player.getPosition().getY());
			writeF(player.getPosition().getZ());
			writeC(player.getPlayerClass().getClassId());
			writeC(player.getGender().getGenderId());
			writeC(player.getLevel());
			writeC(player.isInTeam() ? 3 : player.isLookingForGroup() ? 2 : 0);
			writeS(player.getName(), 56);
		}
	}
}
