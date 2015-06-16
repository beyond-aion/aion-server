package com.aionemu.gameserver.events;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import javolution.util.FastTable;

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

	private Collection<EventListener<T>> listeners = new FastTable<EventListener<T>>(0).shared();
	protected boolean isFirstMethodFill;

	public AbstractEventSource() {
		Class<?> theClass = getClass();
		if (AnnotationManager.containsClass(theClass))
			return;

		isFirstMethodFill = true;
		AnnotatedClass annotatedClass = AnnotationManager.getAnnotatedClass(getClass());
		AnnotatedMethod[] annotated = annotatedClass.getAnnotatedMethods();

		for (AnnotatedMethod method : annotated) {
			if (!addListenable(method))
				continue;
			log.debug("Added method {}", method.getMethod());
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
		Iterator<EventListener<T>> i = listeners.iterator();
		while (i.hasNext()) {
			i.next().onBeforeEvent(event);
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
	 * @return false if the class should not have listeners for the event or calling fireBeforeEvent disabled it by
	 *         setting isHandled to false
	 */
	protected boolean fireAfterEvent(T event, Object[] callingArguments) {
		if (!canHaveEventNotifications(event) || !event.isHandled())
			return false;
		event.callingArguments = callingArguments;
		Iterator<EventListener<T>> i = listeners.iterator();
		while (i.hasNext()) {
			i.next().onAfterEvent(event);
		}
		return true;
	}

	public static final Method getCurrentMethod(Object o) {
		String s = getCallerClass(2).getName();
		Method cm = null;
		for (Method m : o.getClass().getMethods()) {
			if (m.getName().equals(s)) {
				cm = m;
				break;
			}
		}
		return cm;
	}

	/**
	 * Returns the {@link Class} object that contains a caller's method.
	 * 
	 * @param i
	 *          the offset on the call stack of the method of interest
	 * @return the Class found from the calling context, or {@code null} if not found
	 */
	public static Class<?> getCallerClass(int i) {
		Class<?>[] classContext = new SecurityManager() {

			@Override
			public Class<?>[] getClassContext() {
				return super.getClassContext();
			}
		}.getClassContext();

		if (classContext != null) {
			for (int j = 0; j < classContext.length; j++) {
				if (classContext[j] == AbstractEventSource.class) {
					return classContext[i + j];
				}
			}
		}
		else {
			try {
				StackTraceElement[] classNames = Thread.currentThread().getStackTrace();
				for (int j = 0; j < classNames.length; j++) {
					if (Class.forName(classNames[j].getClassName()) == AbstractEventSource.class) {
						return Class.forName(classNames[i + j].getClassName());
					}
				}
			}
			catch (ClassNotFoundException e) {
			}
		}
		return null;
	}

}
