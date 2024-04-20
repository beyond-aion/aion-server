package com.aionemu.gameserver.ai;

import java.util.EnumSet;

import com.aionemu.gameserver.ai.event.AIEventType;

/**
 * @author ATracer, Neon
 */
public enum AIState {
	CREATED(AIEventType.BEFORE_SPAWNED, AIEventType.SPAWNED),
	DIED(AIEventType.DESPAWNED, AIEventType.DROP_REGISTERED),
	DESPAWNED(AIEventType.BEFORE_SPAWNED, AIEventType.SPAWNED),
	IDLE,
	WALKING,
	FOLLOWING,
	RETURNING,
	FIGHT,
	FEAR,
	CONFUSE,
	FORCED_WALKING(AIEventType.MOVE_ARRIVED, AIEventType.MOVE_VALIDATE, AIEventType.DESPAWNED, AIEventType.DIED);

	private final EnumSet<AIEventType> handledAiEvents;

	AIState() {
		this.handledAiEvents = EnumSet.allOf(AIEventType.class);
	}

	AIState(AIEventType first, AIEventType... rest) {
		this.handledAiEvents = EnumSet.of(first, rest);
	}

	/**
	 * @return True, if the given event can be handled in this state.
	 */
	public boolean canHandle(AIEventType event) {
		return handledAiEvents.contains(event);
	}
}
