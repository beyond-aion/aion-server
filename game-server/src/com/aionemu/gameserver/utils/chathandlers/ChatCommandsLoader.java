package com.aionemu.gameserver.utils.chathandlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import com.aionemu.commons.scripting.classlistener.ClassListener;

/**
 * Created on: 12.09.2009 14:13:24
 * 
 * @author Aquanox
 */
public class ChatCommandsLoader implements ClassListener {

	private ChatProcessor processor;

	public ChatCommandsLoader(ChatProcessor processor) {
		this.processor = processor;
	}

	@Override
	public void postLoad(Class<?>[] classes) {
		for (Class<?> c : classes) {
			if (!isValidClass(c))
				continue;
			try {
				processor.registerCommand((ChatCommand) c.getDeclaredConstructor().newInstance());
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void preUnload(Class<?>[] classes) {
	}

	public boolean isValidClass(Class<?> clazz) {
		final int modifiers = clazz.getModifiers();

		if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))
			return false;

		if (!Modifier.isPublic(modifiers))
			return false;

		return ChatCommand.class.isAssignableFrom(clazz);
	}
}
