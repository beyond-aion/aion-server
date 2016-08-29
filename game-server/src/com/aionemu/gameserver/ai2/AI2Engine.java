package com.aionemu.gameserver.ai2;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.commons.scripting.scriptmanager.ScriptManager;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public class AI2Engine implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(AI2Engine.class);
	private ScriptManager scriptManager = new ScriptManager();
	private Map<String, Class<? extends AbstractAI>> aiHandlers = new HashMap<>();

	private AI2Engine() {
	}

	@Override
	public void load(CountDownLatch progressLatch) {
		log.info("AI2 engine load started");

		AggregatedClassListener acl = new AggregatedClassListener();
		acl.addClassListener(new OnClassLoadUnloadListener());
		acl.addClassListener(new ScheduledTaskClassListener());
		acl.addClassListener(new AI2HandlerClassListener());
		scriptManager.setGlobalClassListener(acl);

		try {
			scriptManager.load(new File("./data/scripts/system/aihandlers.xml"));
			validateScripts();
			log.info("Loaded " + aiHandlers.size() + " ai handlers.");
		} catch (Exception e) {
			throw new GameServerError("Can't initialize ai handlers.", e);
		} finally {
			if (progressLatch != null)
				progressLatch.countDown();
		}
	}

	public void reload() {
		shutdown();
		load(null);
	}

	@Override
	public void shutdown() {
		log.info("AI2 engine shutdown started");
		scriptManager.shutdown();
		aiHandlers.clear();
		log.info("AI2 engine shutdown complete");
	}

	public void registerAI(Class<? extends AbstractAI> class1) {
		AIName nameAnnotation = class1.getAnnotation(AIName.class);
		if (nameAnnotation != null) {
			aiHandlers.put(nameAnnotation.value(), class1);
		}
	}

	public final AI2 setupAI(String name, Creature owner) {
		AbstractAI aiInstance = null;
		try {
			aiInstance = aiHandlers.get(name).newInstance();
			aiInstance.setOwner(owner);
			owner.setAi2(aiInstance);
			if (AIConfig.ONCREATE_DEBUG) {
				aiInstance.setLogging(true);
			}
		} catch (Exception e) {
			log.error("[AI2] AI factory error: " + name, e);
		}
		return aiInstance;
	}

	/**
	 * @param aiName
	 * @param owner
	 */
	public void setupAI(AiNames aiName, Npc owner) {
		setupAI(aiName.getName(), owner);
	}

	private void validateScripts() {
		Collection<String> npcAINames = DataManager.NPC_DATA.getNpcData().valueCollection().stream().map(npc -> npc.getAi()).distinct().collect(Collectors.toList());
		for (String name : npcAINames) {
			try {
				aiHandlers.get(name).newInstance();
			} catch (Exception e) {
				log.error("[AI2] AI factory error: " + name, e);
			}
		}
		npcAINames.removeAll(aiHandlers.keySet());
		if (npcAINames.size() > 0) {
			log.warn("Bad AI names: " + String.join(", ", npcAINames));
		}
	}

	public static final AI2Engine getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final AI2Engine instance = new AI2Engine();
	}
}
