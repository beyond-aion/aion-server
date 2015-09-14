package com.aionemu.gameserver.events;

import java.util.EventObject;

/**
 * @author Rolandas
 */
public abstract class AbstractEvent<SourceType> extends EventObject {

	private static final long serialVersionUID = -5493949678727753836L;

	protected Object[] callingArguments;

	private boolean handled;

	public AbstractEvent(SourceType source) {
		super(source);
	}

	@Override
	@SuppressWarnings("unchecked")
	public SourceType getSource() {
		return (SourceType) super.getSource();
	}

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}
}
