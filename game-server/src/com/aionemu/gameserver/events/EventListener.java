package com.aionemu.gameserver.events;

/**
 * @author Rolandas
 */
public interface EventListener<T extends AbstractEvent<?>> {

	public void onBeforeEvent(T event);

	public void onAfterEvent(T event);
}
