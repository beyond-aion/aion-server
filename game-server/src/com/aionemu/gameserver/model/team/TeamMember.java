package com.aionemu.gameserver.model.team;

/**
 * @author ATracer
 */
public interface TeamMember<M> {

	Integer getObjectId();

	String getName();

	M getObject();
}
