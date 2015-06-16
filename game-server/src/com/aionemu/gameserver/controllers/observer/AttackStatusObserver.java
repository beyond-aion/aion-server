package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.controllers.attack.AttackStatus;

/**
 * @author ATracer
 */
public class AttackStatusObserver extends AttackCalcObserver {

	protected int value;
	protected AttackStatus status;

	/**
	 * @param value
	 * @param status
	 */
	public AttackStatusObserver(int value, AttackStatus status) {
		this.value = value;
		this.status = status;
	}
}
