package com.aionemu.gameserver.services.siegeservice;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.abyss.AddAPGlobalCallback;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Estrayl
 */
public class AgentAbyssPointsListener extends AddAPGlobalCallback {

	private final Siege<?> siege;

	public AgentAbyssPointsListener(Siege<?> siege) {
		this.siege = siege;
	}

	@Override
	public void onAbyssPointsAdded(Player player, int abyssPoints) {	
		if (player.isInsideZone(ZoneName.get("FLAMEBERTH_DOWNS_600100000"))	|| player.isInsideZone(ZoneName.get("DRAGON_LORDS_SHRINE_600100000")))
			siege.addAbyssPoints(player, abyssPoints);
	}
}
