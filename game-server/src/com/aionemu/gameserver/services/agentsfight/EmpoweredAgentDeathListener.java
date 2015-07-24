package com.aionemu.gameserver.services.agentsfight;

import com.aionemu.gameserver.ai2.GeneralAIEvent;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;

/**
 * @author Yeats
 *
 */
public class EmpoweredAgentDeathListener extends OnDieEventListener {

	
	private final NpcAI2 owner;
	
	public EmpoweredAgentDeathListener(NpcAI2 ai) {
		this.owner = ai;
	}

	@Override
	public void onBeforeEvent(GeneralAIEvent event) {
		super.onBeforeEvent(event);
		if (event.isHandled()) {
			AgentsFightService.getInstance().stop(owner);	
		}
	}
}
