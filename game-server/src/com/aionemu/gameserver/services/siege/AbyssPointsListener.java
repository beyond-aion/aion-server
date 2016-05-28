package com.aionemu.gameserver.services.siege;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.services.abyss.AddAPGlobalCallback;

/**
 * @author SoulKeeper, Source
 */
public class AbyssPointsListener extends AddAPGlobalCallback {

	private final Siege<?> siege;

	public AbyssPointsListener(Siege<?> siege) {
		this.siege = siege;
	}

	@Override
	public void onAbyssPointsAdded(Player player, int abyssPoints) {
		SiegeLocation fortress = siege.getSiegeLocation();

		// Make sure that only AP earned near this fortress will be added
		// Abyss points can be added only while in the siege zones
		if (fortress.isInsideLocation(player)) {
			siege.addAbyssPoints(player, abyssPoints);
		}
	}

}
