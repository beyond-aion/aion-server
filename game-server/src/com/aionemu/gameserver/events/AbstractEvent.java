package com.aionemu.gameserver.events;

import java.util.Objects;

/**
 * @author Rolandas
 */
public abstract class AbstractEvent<T> {

	protected T source;
	private boolean handled;

	public AbstractEvent(T source) {
		this.source = Objects.requireNonNull(source);
	}

	public T getSource() {
		return source;
	}

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}
}
