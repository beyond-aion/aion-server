package com.aionemu.gameserver.model.actions;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.services.RespawnService;

/**
 * @author Neon
 */
public class VisibleObjectActions {

	public static String getName(VisibleObject obj) {
		return obj.getName();
	}

	/**
	 * Despawns the object from the world (without scheduling respawn).
	 * 
	 * @param obj
	 */
	public static void delete(VisibleObject obj) {
		delete(obj, false);
	}

	/**
	 * Deletes the object and optionally schedules respawn.
	 * 
	 * @param obj
	 * @param scheduleRespawn
	 */
	public static void delete(VisibleObject obj, boolean scheduleRespawn) {
		if (obj != null) {
			obj.getController().onDelete();
			if (scheduleRespawn)
				RespawnService.scheduleRespawnTask(obj);
		}
	}
}
