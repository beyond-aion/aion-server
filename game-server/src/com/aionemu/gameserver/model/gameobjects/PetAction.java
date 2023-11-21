package com.aionemu.gameserver.model.gameobjects;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ATracer
 */
public enum PetAction {
	LOAD_PETS(0),
	ADOPT(1),
	SURRENDER(2),
	SPAWN(3),
	DISMISS(4),
	TALK_WITH_MERCHANT(6),
	TALK_WITH_MINDER(7),
	FOOD(9),
	RENAME(10),
	MOOD(12),
	SPECIAL_FUNCTION(13),
	EXTEND_EXPIRATION(15),
	H_ADOPT(16),
	H_ABANDON(17),
	UNKNOWN(255);

	private static final Map<Integer, PetAction> petActions = new HashMap<>();

	static {
		for (PetAction action : values()) {
			petActions.put(action.getActionId(), action);
		}
	}

	private int actionId;

	private PetAction(int actionId) {
		this.actionId = actionId;
	}

	public int getActionId() {
		return actionId;
	}

	public static PetAction getActionById(int actionId) {
		PetAction action = petActions.get(actionId);
		return action != null ? action : UNKNOWN;
	}
}
