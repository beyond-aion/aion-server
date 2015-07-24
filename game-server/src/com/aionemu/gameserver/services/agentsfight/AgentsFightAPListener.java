package com.aionemu.gameserver.services.agentsfight;

import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.abyss.AddAPGlobalCallback;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Yeats
 *
 */
@SuppressWarnings("rawtypes")
public class AgentsFightAPListener extends AddAPGlobalCallback {
	
	@Override
	public CallbackResult afterCall(Object obj, Object[] args, Object methodResult) {
		Player player = (Player) args[0];
		VisibleObject creature = (VisibleObject) args[1];
		int abyssPoints = (Integer) args[2];

		// Only Players can add points
		if (creature instanceof Player) {
			onAbyssPointsAdded(player, abyssPoints);
		}

		return CallbackResult.newContinue();
	}

	@Override
	public void onAbyssPointsAdded(Player player, int abyssPoints) {
		if (player.isInsideZone(ZoneName.get("FLAMEBERTH_DOWNS_600100000")) || player.isInsideZone(ZoneName.get("DRAGON_LORDS_SHRINE_600100000"))) {
			AgentsFightService.getInstance().addAP(player, abyssPoints);
		} 
	}

}
