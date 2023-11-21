package com.aionemu.gameserver.ai;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.ScriptManager;
import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

/**
 * @author ATracer
 */
public class AIEngine implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(AIEngine.class);
	private ScriptManager scriptManager = new ScriptManager();
	private Map<String, Class<? extends AbstractAI<? extends Creature>>> aiHandlers = new HashMap<>();

	private AIEngine() {
	}

	@Override
	public void load() {
		log.info("AI engine load started");

		AggregatedClassListener acl = new AggregatedClassListener();
		acl.addClassListener(new OnClassLoadUnloadListener());
		acl.addClassListener(new ScheduledTaskClassListener());
		acl.addClassListener(new AIHandlerClassListener());
		scriptManager.setGlobalClassListener(acl);

		try {
			scriptManager.load(AIConfig.HANDLER_DIRECTORY);
			validateScripts();
			log.info("Loaded " + aiHandlers.size() + " ai handlers.");
		} catch (Exception e) {
			throw new GameServerError("Can't initialize ai handlers.", e);
		}
	}

	public void reload() {
		shutdown();
		load();
	}

	@Override
	public void shutdown() {
		log.info("AI engine shutdown started");
		scriptManager.shutdown();
		aiHandlers.clear();
		log.info("AI engine shutdown complete");
	}

	public void registerAI(Class<AbstractAI<? extends Creature>> aiClass) {
		AIName nameAnnotation = aiClass.getAnnotation(AIName.class);
		if (nameAnnotation != null) {
			Class<?> presentClass = aiHandlers.putIfAbsent(nameAnnotation.value(), aiClass);
			if (presentClass != null)
				throw new IllegalArgumentException("Duplicate AIs with name " + nameAnnotation.value() + " (" + aiClass + ", " + presentClass + ")");
		}
	}

	public <T extends Creature> AbstractAI<? extends Creature> newAI(String name, T owner) {
		AbstractAI<? extends Creature> aiInstance;
		if (name == null) {
			aiInstance = new DummyAI<>(owner);
		} else {
			Class<? extends AbstractAI<? extends Creature>> aiClass = aiHandlers.get(name);
			if (aiClass == null)
				throw new IllegalArgumentException("No AI found for name " + name);
			Constructor<? extends AbstractAI<? extends Creature>> constructor = findConstructor(aiClass, owner.getClass(), false);
			if (constructor == null)
				throw new IllegalArgumentException(aiClass + " cannot be instantiated with " + owner.getClass().getSimpleName() + " as the owner");
			try {
				aiInstance = constructor.newInstance(owner);
			} catch (Exception e) {
				throw new IllegalArgumentException("Could not instantiate AI for class " + aiClass + " (owner: " + owner + ")", e);
			}
		}
		if (AIConfig.ONCREATE_DEBUG)
			aiInstance.setLogging(true);
		return aiInstance;
	}

	@SuppressWarnings("unchecked")
	private <T> Constructor<T> findConstructor(Class<T> aiClass, Class<? extends Creature> searchParamType, boolean isSuperType) {
		for (Constructor<?> constructor : aiClass.getDeclaredConstructors()) {
			if (constructor.getParameterCount() == 1) {
				Class<?> constructorParamType = constructor.getParameterTypes()[0];
				if (isSuperType) {
					if (searchParamType.isAssignableFrom(constructorParamType))
						return (Constructor<T>) constructor;
				} else if (constructorParamType.isAssignableFrom(searchParamType)) {
					return (Constructor<T>) constructor;
				}
			}
		}
		return null;
	}

	private void validateScripts() {
		aiHandlers.values().forEach(aiClass -> {
			Class<? extends Creature> ownerType = findDefaultOwnerType(aiClass);
			if (ownerType == null)
				throw new GameServerError("Faulty AI handler: " + aiClass + " is missing generic owner type info");
			if (findConstructor(aiClass, ownerType, true) == null)
				throw new GameServerError(
					"Faulty AI handler: " + aiClass + " is missing constructor taking owner of type " + ownerType + " as the only argument");
		});
		Set<String> npcAINames = DataManager.NPC_DATA.getNpcData().stream().map(NpcTemplate::getAiName).filter(Objects::nonNull).collect(Collectors.toSet());
		npcAINames.removeAll(aiHandlers.keySet());
		if (!npcAINames.isEmpty())
			throw new GameServerError("No AIs could be found for the following npc_template AI names: " + String.join(", ", npcAINames));
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Creature> findDefaultOwnerType(Class<? extends AbstractAI<? extends Creature>> aiClass) {
		Class<?> currentClass = aiClass;
		while (currentClass.getSuperclass() != AbstractAI.class) {
			Type genericSuperClass = currentClass.getGenericSuperclass();
			if (genericSuperClass instanceof ParameterizedType ownerTypeHolder) {
				if (ownerTypeHolder.getActualTypeArguments().length == 1) {
					Type type = ownerTypeHolder.getActualTypeArguments()[0];
					if (type instanceof TypeVariable<?>)
						type = ((TypeVariable<?>) type).getBounds()[0];
					if (type instanceof Class && Creature.class.isAssignableFrom((Class<?>) type))
						return (Class<? extends Creature>) type;
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return null;
	}

	public static AIEngine getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final AIEngine instance = new AIEngine();
	}

	private static class DummyAI<T extends Creature> extends AITemplate<T> {

		private DummyAI(T owner) {
			super(owner);
		}
	}
}
