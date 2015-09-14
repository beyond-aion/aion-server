package com.aionemu.gameserver.questEngine.handlers;

/**
 * @author Rolandas
 */
public enum HandlerResult {
	UNKNOWN, // allow other handlers to process
	SUCCESS,
	FAILED;

	public static HandlerResult fromBoolean(Boolean value) {
		if (value == null)
			return HandlerResult.UNKNOWN;
		else if (value)
			return HandlerResult.SUCCESS;
		return HandlerResult.FAILED;
	}
}
