package com.aionemu.gameserver.ai.event;

/**
 * @author ATracer
 */
public enum AIEventType {
	NONE,
	ACTIVATE,
	DEACTIVATE,
	FREEZE,
	UNFREEZE,

	/**
	 * Creature is being attacked (internal)
	 */
	ATTACK,
	/**
	 * Creature's attack part is complete (internal)
	 */
	ATTACK_COMPLETE,
	/**
	 * Creature's stopping attack (internal)
	 */
	ATTACK_FINISH,
	/**
	 * Some neighbour creature is being attacked (broadcast)
	 */
	CREATURE_NEEDS_SUPPORT,
	CREATURE_NEEDS_HELP,

	MOVE_VALIDATE,
	MOVE_ARRIVED,

	CREATURE_SEE,
	CREATURE_NOT_SEE,
	CREATURE_MOVED,
	CREATURE_AGGRO,
	BEFORE_SPAWNED,
	SPAWNED,
	DESPAWNED,
	DIED,

	TARGET_TOOFAR,
	TARGET_GIVEUP,
	TARGET_CHANGED,
	FOLLOW_ME,
	STOP_FOLLOW_ME,

	NOT_AT_HOME,
	BACK_HOME,

	DIALOG_START,
	DIALOG_FINISH,

	DROP_REGISTERED
}
