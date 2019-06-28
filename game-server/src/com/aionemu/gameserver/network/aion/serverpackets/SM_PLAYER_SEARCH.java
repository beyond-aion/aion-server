package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.AbstractPlayerInfoPacket.CHARNAME_MAX_LENGTH;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.DeniedStatus;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.utils.ChatUtil;

/**
 * Sent to fill the search panel of a players social window<br />
 * I.E.: In response to a <tt>CM_PLAYER_SEARCH</tt>
 * 
 * @author Ben
 */
public class SM_PLAYER_SEARCH extends AionServerPacket {

	private List<Player> players;

	/**
	 * Constructs a new packet that will send these players
	 * 
	 * @param players
	 *          List of players to show
	 */
	public SM_PLAYER_SEARCH(List<Player> players) {
		this.players = players;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player activePlayer = con.getActivePlayer();
		writeH(players.size());
		for (Player player : players) {
			writeD(player.getWorldId());
			writeF(player.getX());
			writeF(player.getY());
			writeF(player.getZ());
			writeC(player.getPlayerClass().getClassId());
			writeC(player.getGender().getGenderId());
			writeC(player.getLevel());
			writeC(player.getPlayerSettings().isInDeniedStatus(DeniedStatus.GROUP) ? 1 : player.isInTeam() ? 3 : player.isLookingForGroup() ? 2 : 0);
			writeS(ChatUtil.toFactionPrefixedName(activePlayer, player), CHARNAME_MAX_LENGTH + 2);
		}
	}
}
