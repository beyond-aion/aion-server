package com.aionemu.gameserver.model.templates.zone;

/**
 * @author MrPoke
 */
public enum ZoneType {

	FLY,
	NO_FLY,
	SIEGE,
	PVP,
	// Client-side disablePvP area: hostile-race players are neutral to each other inside it.
	DISABLE_PVP
}
