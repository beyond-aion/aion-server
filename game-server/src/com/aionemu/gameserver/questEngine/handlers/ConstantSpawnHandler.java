package com.aionemu.gameserver.questEngine.handlers;

import java.util.HashSet;

/**
 * @author Rolandas
 */
public interface ConstantSpawnHandler {

	public int getQuestId();

	public HashSet<Integer> getNpcIds();
}
