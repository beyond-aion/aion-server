package com.aionemu.gameserver.model.team2;

/**
 * @author ATracer
 */
public interface TeamMember<M> {

	Integer getObjectId();

	String getName();

	M getObject();
}
