package com.aionemu.gameserver.ai2;

import java.util.EnumSet;

import com.aionemu.gameserver.ai2.event.AIEventType;

/**
 * @author ATracer
 * @modified Neon
 */
public enum AIState {
	CREATED(AIEventType.SPAWNED),
	DIED(AIEventType.DESPAWNED, AIEventType.DROP_REGISTERED),
	DESPAWNED(AIEventType.BEFORE_SPAWNED, AIEventType.SPAWNED),
	IDLE,
	WALKING,
	FOLLOWING,
	RETURNING,
	FIGHT,
	FEAR,
	FORCED_WALKING;

	private final EnumSet<AIEventType> handledAiEvents;

	private AIState() {
		this.handledAiEvents = EnumSet.allOf(AIEventType.class);
	}

	private AIState(AIEventType first, AIEventType... rest) {
		this.handledAiEvents = EnumSet.of(first, rest);
	}

	/**
	 * @param event
	 * @return True, if the given event can be handled in this state.
	 */
	public boolean canHandle(AIEventType event) {
		return handledAiEvents.contains(event);
	}
}
