package com.aionemu.gameserver.model.team;

/**
 * @author ATracer
 */
public interface TeamEvent {

	void handleEvent();

	boolean checkCondition();
}
