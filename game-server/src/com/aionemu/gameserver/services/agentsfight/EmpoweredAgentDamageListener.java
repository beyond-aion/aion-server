package com.aionemu.gameserver.services.agentsfight;

import com.aionemu.gameserver.controllers.attack.AddDamageEvent;
import com.aionemu.gameserver.controllers.attack.AddDamageEventListener;

/**
 * @author Yeats
 *
 */
public class EmpoweredAgentDamageListener extends AddDamageEventListener {

	@Override
	public void onAfterEvent(AddDamageEvent event) {
		if (event.isHandled()) {
			AgentsFightService.getInstance().addDamage(event.getAttacker(), event.getDamage());
		}
	}

}
