package com.aionemu.gameserver.instance;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.ScriptManager;
import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.main.InstanceConfig;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author ATracer
 */
public class InstanceEngine implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(InstanceEngine.class);
	private ScriptManager scriptManager = new ScriptManager();
	private Map<Integer, Class<? extends InstanceHandler>> instanceHandlers = new HashMap<>();

	@Override
	public void load() {
		log.info("Instance engine load started");

		AggregatedClassListener acl = new AggregatedClassListener();
		acl.addClassListener(new OnClassLoadUnloadListener());
		acl.addClassListener(new ScheduledTaskClassListener());
		acl.addClassListener(new InstanceHandlerClassListener());
		scriptManager.setGlobalClassListener(acl);

		try {
			scriptManager.load(InstanceConfig.HANDLER_DIRECTORY);
			log.info("Loaded " + instanceHandlers.size() + " instance handlers.");
		} catch (Exception e) {
			throw new GameServerError("Can't initialize instance handlers.", e);
		}
	}

	@Override
	public void shutdown() {
		log.info("Instance engine shutdown started");
		scriptManager.shutdown();
		instanceHandlers.clear();
		log.info("Instance engine shutdown complete");
	}

	public InstanceHandler getNewInstanceHandler(WorldMapInstance instance) {
		Class<? extends InstanceHandler> handlerClass = instanceHandlers.get(instance.getMapId());
		InstanceHandler instanceHandler = null;
		if (handlerClass != null) {
			try {
				instanceHandler = handlerClass.getDeclaredConstructor(WorldMapInstance.class).newInstance(instance);
			} catch (Exception ex) {
				log.warn("Can't instantiate instance handler for map " + instance.getMapId() + " (instanceId: " + instance.getInstanceId() + ')', ex);
			}
		}

		return instanceHandler != null ? instanceHandler : new GeneralInstanceHandler(instance);
	}

	final void addInstanceHandlerClass(Class<? extends InstanceHandler> handler) {
		InstanceID idAnnotation = handler.getAnnotation(InstanceID.class);
		if (idAnnotation != null) {
			instanceHandlers.put(idAnnotation.value(), handler);
		}
	}

	public static InstanceEngine getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final InstanceEngine instance = new InstanceEngine();
	}
}
