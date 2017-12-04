package com.aionemu.gameserver.instance;

import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.ClassListener;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;

/**
 * @author ATracer
 */
public class InstanceHandlerClassListener implements ClassListener {

	private static final Logger log = LoggerFactory.getLogger(InstanceHandlerClassListener.class);

	@SuppressWarnings("unchecked")
	@Override
	public void postLoad(Class<?>[] classes) {
		for (Class<?> c : classes) {
			if (log.isDebugEnabled())
				log.debug("Load class " + c.getName());

			if (!isValidClass(c))
				continue;

			if (InstanceHandler.class.isAssignableFrom(c))
				InstanceEngine.getInstance().addInstanceHandlerClass((Class<? extends InstanceHandler>) c);
		}
	}

	@Override
	public void preUnload(Class<?>[] classes) {
		if (log.isDebugEnabled()) {
			for (Class<?> c : classes)
				log.debug("Unload class " + c.getName());
		}
	}

	public boolean isValidClass(Class<?> clazz) {
		final int modifiers = clazz.getModifiers();

		if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))
			return false;

		if (!Modifier.isPublic(modifiers))
			return false;

		return true;
	}
}
