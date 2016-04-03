package com.aionemu.gameserver.ai2;

import java.util.Arrays;
import java.util.EnumSet;

import com.aionemu.gameserver.ai2.event.AIEventType;

/**
 * @author ATracer
 */
public enum StateEvents {
	CREATED_EVENTS(AIEventType.SPAWNED),
	DESPAWN_EVENTS(AIEventType.BEFORE_SPAWNED, AIEventType.SPAWNED),
	DEAD_EVENTS(AIEventType.DESPAWNED, AIEventType.DROP_REGISTERED);

	private EnumSet<AIEventType> events;

	private StateEvents(AIEventType... aiEventTypes) {
		this.events = EnumSet.copyOf(Arrays.asList(aiEventTypes));
	}

	public boolean hasEvent(AIEventType event) {
		return events.contains(event);
	}

}
