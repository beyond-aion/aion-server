package com.aionemu.gameserver.questEngine.handlers;

import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.ClassListener;
import com.aionemu.commons.utils.ClassUtils;
import com.aionemu.gameserver.questEngine.QuestEngine;

/**
 * @author MrPoke
 */
public class QuestHandlerLoader implements ClassListener {

	private static final Logger logger = LoggerFactory.getLogger(QuestHandlerLoader.class);

	public QuestHandlerLoader() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postLoad(Class<?>[] classes) {
		for (Class<?> c : classes) {
			if (c == null)
				continue;
			if (logger.isDebugEnabled())
				logger.debug("Load class " + c.getName());

			if (!isValidClass(c))
				continue;

			if (ClassUtils.isSubclass(c, QuestHandler.class)) {
				try {
					Class<? extends QuestHandler> tmp = (Class<? extends QuestHandler>) c;
					if (tmp != null)
						QuestEngine.getInstance().addQuestHandler(tmp.newInstance());
				} catch (Exception e) {
					throw new RuntimeException("Failed to load quest handler class: " + c.getName(), e);
				}
			}
		}
	}

	@Override
	public void preUnload(Class<?>[] classes) {
		if (logger.isDebugEnabled())
			for (Class<?> c : classes)
				// debug messages
				logger.debug("Unload class " + c.getName());

		QuestEngine.getInstance().clear();
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
