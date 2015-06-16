package com.aionemu.gameserver.model.actions;

import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 *
 * @author xTz
 */
public class NpcActions extends CreatureActions {

	public static void scheduleRespawn(Npc npc){
		npc.getController().scheduleRespawn();
	}
}
