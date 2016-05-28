package com.aionemu.gameserver.services.siege;

import com.aionemu.gameserver.controllers.attack.AddDamageEvent;
import com.aionemu.gameserver.controllers.attack.AddDamageEventListener;

/**
 * @author SoulKeeper
 */
public class SiegeBossDoAddDamageListener extends AddDamageEventListener {

	private final Siege<?> siege;

	public SiegeBossDoAddDamageListener(Siege<?> siege) {
		this.siege = siege;
	}

	@Override
	public void onAfterEvent(AddDamageEvent event) {
		if (event.isHandled())
			siege.addBossDamage(event.getAttacker(), event.getDamage());
	}

}
