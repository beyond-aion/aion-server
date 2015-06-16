package com.aionemu.gameserver.model.team2;

/**
 * @author ATracer
 */
public interface TeamEvent {

	void handleEvent();
	
	boolean checkCondition();
}
