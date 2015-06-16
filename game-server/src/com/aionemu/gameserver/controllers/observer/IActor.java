package com.aionemu.gameserver.controllers.observer;

/**
 * @author Rolandas
 */
public interface IActor {

	void act();
	
	void setEnabled(boolean enable);

	void abort();
}
