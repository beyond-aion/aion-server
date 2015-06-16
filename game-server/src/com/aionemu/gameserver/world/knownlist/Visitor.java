package com.aionemu.gameserver.world.knownlist;

/**
 * @author ATracer
 */
public interface Visitor<T> {

	void visit(T object);
}
