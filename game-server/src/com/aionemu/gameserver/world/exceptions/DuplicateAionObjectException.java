package com.aionemu.gameserver.world.exceptions;

import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * This Exception will be thrown when some AionObject will be stored more then one time. This Exception indicating serious error.
 * 
 * @author -Nemesiss-
 */
public class DuplicateAionObjectException extends RuntimeException {

	private static final long serialVersionUID = -2031489557355197834L;

	/**
	 * Constructs an <code>DuplicateAionObjectException</code> for the given objects
	 */
	public DuplicateAionObjectException(AionObject object, AionObject presentObject) {
		super(createMessage(object, presentObject));
	}

	private static String createMessage(AionObject object, AionObject presentObject) {
		StringBuilder sb = new StringBuilder("Duplicate object: ");
		sb.append(object);
		if (object instanceof Player)
			sb.append(' ').append(((Player) object).getPosition());
		sb.append(", already present object: ");
		sb.append(presentObject);
		if (presentObject instanceof Player)
			sb.append(' ').append(((Player) presentObject).getPosition());
		return sb.toString();
	}
}
