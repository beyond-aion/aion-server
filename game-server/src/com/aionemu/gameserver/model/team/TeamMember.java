package com.aionemu.gameserver.model.team;

/**
 * @author ATracer
 */
public interface TeamMember<M> {

	int getObjectId();

	String getName();

	M getObject();
}
