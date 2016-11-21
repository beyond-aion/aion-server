package com.aionemu.gameserver.world.exceptions;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * This exception will be thrown when object attempts to spawn in world, but is already spawned.
 * 
 * @author -Nemesiss-
 */
public class AlreadySpawnedException extends RuntimeException {

	private static final long serialVersionUID = -3065200842198146682L;

	/**
	 * Constructs an <code>AlreadySpawnedException</code> for the given object
	 */
	public AlreadySpawnedException(VisibleObject object) {
		super(createMessage(object));
	}

	private static String createMessage(VisibleObject object) {
		StringBuilder sb = new StringBuilder(object.getClass().getSimpleName());
		sb.append(" ");
		sb.append(object.getName());
		if (object.getObjectTemplate() != null && !(object instanceof Player))
			sb.append(" (ID: ").append(object.getObjectTemplate().getTemplateId()).append(")");
		sb.append(" is already spawned at ");
		sb.append(object.getPosition());
		return sb.toString();
	}
}
