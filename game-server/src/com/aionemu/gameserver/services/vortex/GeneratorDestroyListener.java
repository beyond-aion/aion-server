package com.aionemu.gameserver.services.vortex;

import com.aionemu.gameserver.ai.GeneralAIEvent;
import com.aionemu.gameserver.ai.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.services.VortexService;

/**
 * @author Source
 */
public class GeneratorDestroyListener extends OnDieEventListener {

	private final DimensionalVortex<?> vortex;

	public GeneratorDestroyListener(DimensionalVortex<?> vortex) {
		this.vortex = vortex;
	}

	@Override
	public void onAfterEvent(GeneralAIEvent event) {
		if (event.isHandled()) {
			vortex.setGeneratorDestroyed(true);
			VortexService.getInstance().stopInvasion(vortex.getVortexLocationId());
		}
	}

}
