package com.aionemu.gameserver.world.knownlist;

/**
 * @author ATracer
 */
public interface VisitorWithOwner<T, V> {

	void visit(T object, V owner);
}
