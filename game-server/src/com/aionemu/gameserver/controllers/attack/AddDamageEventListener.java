package com.aionemu.gameserver.controllers.attack;

import com.aionemu.gameserver.events.EventListener;

/**
 * @author Rolandas
 */
public abstract class AddDamageEventListener implements EventListener<AddDamageEvent> {

	@Override
	public void onBeforeEvent(AddDamageEvent event) {
		if (event.getSource().isAware(event.getAttacker())) {
			event.setHandled(true);
		}
	}

}
