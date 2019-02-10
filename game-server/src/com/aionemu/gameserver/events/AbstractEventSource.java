package com.aionemu.gameserver.events;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.utils.annotations.AnnotatedClass;
import com.aionemu.gameserver.utils.annotations.AnnotatedMethod;
import com.aionemu.gameserver.utils.annotations.AnnotationManager;

/**
 * @author Rolandas
 */
public abstract class AbstractEventSource<T extends AbstractEvent<?>> {

	private static Logger log = LoggerFactory.getLogger(AbstractEventSource.class);

	private Collection<EventListener<T>> listeners = new CopyOnWriteArrayList<>();

	public AbstractEventSource() {
		Class<?> theClass = getClass();
		AnnotatedClass annotatedClass;
		synchronized (theClass) {
			if (AnnotationManager.containsClass(theClass))
				return;

			annotatedClass = AnnotationManager.getAnnotatedClass(theClass);

			for (AnnotatedMethod method : annotatedClass.getAnnotatedMethods()) {
				if (addListenable(method))
					log.debug("Added method {}", method.getMethod());
			}
		}
	}

	protected abstract boolean addListenable(AnnotatedMethod annotatedMethod);

	public void addEventListener(EventListener<T> listener) {
		listeners.add(listener);
	}

	public void removeEventListener(EventListener<T> listener) {
		listeners.remove(listener);
	}

	public boolean hasSubscribers() {
		return listeners.size() > 0;
	}

	protected abstract boolean canHaveEventNotifications(T event);

	/**
	 * Method to notify all listeners about the event before it is actually handled
	 * 
	 * @param event
	 * @return true if notified
	 */
	protected boolean fireBeforeEvent(T event) {
		return fireBeforeEvent(event, null);
	}

	/**
	 * Method to pass event arguments to event listener. Override it in class and call it from event method
	 * 
	 * @param event
	 * @param callingArguments
	 * @return false if the class should not have listeners for the event
	 */
	protected boolean fireBeforeEvent(T event, Object[] callingArguments) {
		if (!canHaveEventNotifications(event))
			return false;
		event.callingArguments = callingArguments;
		for (EventListener<T> listener : listeners) {
			listener.onBeforeEvent(event);
		}
		return true;
	}

	/**
	 * Method to notify all listeners about the event after the handler was called
	 * 
	 * @param event
	 * @return true if notified
	 */
	protected boolean fireAfterEvent(T event) {
		return fireAfterEvent(event, null);
	}

	/**
	 * Method to pass event arguments to event listener. Override it in class and call it from event method
	 * 
	 * @param event
	 * @param callingArguments
	 * @return false if the class should not have listeners for the event or calling fireBeforeEvent disabled it by setting isHandled to false
	 */
	protected boolean fireAfterEvent(T event, Object[] callingArguments) {
		if (!canHaveEventNotifications(event) || !event.isHandled())
			return false;
		event.callingArguments = callingArguments;
		for (EventListener<T> listener : listeners) {
			listener.onAfterEvent(event);
		}
		return true;
	}

}
