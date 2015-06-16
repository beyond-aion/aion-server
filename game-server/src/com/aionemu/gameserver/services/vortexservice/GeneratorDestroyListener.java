package com.aionemu.gameserver.services.vortexservice;

import com.aionemu.gameserver.ai2.GeneralAIEvent;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;
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
	public void onBeforeEvent(GeneralAIEvent event) {
		super.onBeforeEvent(event);
	}

	@Override
	public void onAfterEvent(GeneralAIEvent event) {
		if (event.isHandled()) {
			vortex.setGeneratorDestroyed(true);
			VortexService.getInstance().stopInvasion(vortex.getVortexLocationId());
		}
	}

}
