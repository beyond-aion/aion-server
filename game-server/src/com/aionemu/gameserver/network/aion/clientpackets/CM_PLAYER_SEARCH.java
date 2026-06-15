package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_SEARCH;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.world.World;

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

	public CM_PLAYER_SEARCH(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		name = Util.convertName(readS(25));
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

		if (activePlayer.getLevel() < CustomConfig.LEVEL_TO_SEARCH) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_CANT_WHO_LEVEL(CustomConfig.LEVEL_TO_SEARCH));
			return;
		}

		List<Player> matches = new ArrayList<>();
		for (Player player : World.getInstance().getAllPlayers()) {
			if (!activePlayer.isStaff()) { // staff can find all players
				if (player.getRace() != activePlayer.getRace() && !CustomConfig.FACTIONS_SEARCH_MODE)
					continue;
				if (player.getFriendList().getStatus() == Status.OFFLINE)
					continue;
				if (player.isStaff() && !CustomConfig.SEARCH_GM_LIST)
					continue;
			}
			if (lfgOnly == 1 && !player.isLookingForGroup())
				continue;
			if (!name.isEmpty() && !player.getName().toLowerCase().contains(name.toLowerCase()))
				continue;
			if (minLevel != 0xFF && player.getLevel() < minLevel)
				continue;
			if (maxLevel != 0xFF && player.getLevel() > maxLevel)
				continue;
			if (classMask > 0 && (1 << player.getPlayerClass().getClassId() & classMask) == 0)
				continue;
			if (region > 0 && player.getWorldId() != region)
				continue;
			if (player.equals(activePlayer))
				continue;

			matches.add(player);

			if (matches.size() == MAX_RESULTS)
				break;
		}

		sendPacket(new SM_PLAYER_SEARCH(matches));
	}

}
