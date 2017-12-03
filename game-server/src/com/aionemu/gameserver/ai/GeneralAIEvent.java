package com.aionemu.gameserver.ai;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.events.AbstractEvent;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Rolandas
 */
public class GeneralAIEvent extends AbstractEvent<AbstractAI<? extends Creature>> {

	private static final long serialVersionUID = 5936695693551359627L;

	private final AIEventType eventType;

	public GeneralAIEvent(AbstractAI<? extends Creature> source, AIEventType eventType) {
		super(source);
		this.eventType = eventType;
	}

	public AIEventType getEventType() {
		return eventType;
	}

}
