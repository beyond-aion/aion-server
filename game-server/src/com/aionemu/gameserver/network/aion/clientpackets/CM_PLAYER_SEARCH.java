package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Iterator;
import java.util.List;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_SEARCH;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.world.World;

import javolution.util.FastTable;

/**
 * Received when a player searches using the social search panel
 * 
 * @author Ben
 */
public class CM_PLAYER_SEARCH extends AionClientPacket {

	/**
	 * The max number of players to return as results
	 */
	public static final int MAX_RESULTS = 104; // 3.0

	private String name;
	private int region;
	private int classMask;
	private int minLevel;
	private int maxLevel;
	private int lfgOnly;

	public CM_PLAYER_SEARCH(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		name = Util.convertName(readS(52));
		region = readD();
		classMask = readD();
		minLevel = readUC();
		maxLevel = readUC();
		lfgOnly = readUC();
		readC(); // 0x00 in search pane 0x30 in /who?
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();

		Iterator<Player> it = World.getInstance().getPlayersIterator();
		List<Player> matches = new FastTable<>();

		if (activePlayer.getLevel() < CustomConfig.LEVEL_TO_SEARCH) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_CANT_WHO_LEVEL(CustomConfig.LEVEL_TO_SEARCH));
			return;
		}

		while (it.hasNext() && matches.size() < MAX_RESULTS) {
			Player player = it.next();
			if (player.getRace() != activePlayer.getRace() && !CustomConfig.FACTIONS_SEARCH_MODE)
				continue;
			else if (!player.getCommonData().isOnline() || player.getFriendList().getStatus() == Status.OFFLINE)
				continue;
			else if (!player.isSpawned())
				continue;
			else if (player.isGM() && !CustomConfig.SEARCH_GM_LIST)
				continue;
			else if (lfgOnly == 1 && !player.isLookingForGroup())
				continue;
			else if (!name.isEmpty() && !player.getName().toLowerCase().contains(name.toLowerCase()))
				continue;
			else if (minLevel != 0xFF && player.getLevel() < minLevel)
				continue;
			else if (maxLevel != 0xFF && player.getLevel() > maxLevel)
				continue;
			else if (classMask > 0 && (player.getPlayerClass().getMask() & classMask) == 0)
				continue;
			else if (region > 0 && player.getWorldId() != region)
				continue;
			matches.add(player);
		}

		sendPacket(new SM_PLAYER_SEARCH(matches));
	}

}
