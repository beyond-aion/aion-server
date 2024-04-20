package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is informing client that some AionObject is no longer visible.
 *
 * @author -Nemesiss-, Neon
 */
public class SM_DELETE extends AionServerPacket {

	/**
	 * Object that is no longer visible.
	 */
	private final int objectId;

	/**
	 * Animation that will be seen before the object disappears.
	 */
	private final int animationId;

	public SM_DELETE(VisibleObject object) {
		this(object, ObjectDeleteAnimation.FADE_OUT, true);
	}

	public SM_DELETE(VisibleObject object, boolean inRange) {
		this(object, ObjectDeleteAnimation.FADE_OUT, inRange);
	}

	public SM_DELETE(VisibleObject object, ObjectDeleteAnimation animation) {
		this(object, animation, true);
	}

	private SM_DELETE(VisibleObject object, ObjectDeleteAnimation animation, boolean inRange) {
		this.objectId = object.getObjectId();
		this.animationId = inRange ? animation.getId() : ObjectDeleteAnimation.NONE.getId();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeC(animationId);
	}
}
