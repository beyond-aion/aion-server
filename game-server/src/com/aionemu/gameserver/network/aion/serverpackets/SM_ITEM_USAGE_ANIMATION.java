package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.items.ItemUseAnimation;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_ITEM_USAGE_ANIMATION extends AionServerPacket {

	private final int playerObjId;
	private final int targetObjId;
	private final int itemObjId;
	private final int itemId;
	private final int castTime;
	private final ItemUseAnimation animation;
	private final boolean suppressAnimation;

	public SM_ITEM_USAGE_ANIMATION(int playerObjId, int itemObjId, int itemId) {
		this(playerObjId, playerObjId, itemObjId, itemId, 0, ItemUseAnimation.USE_SUCCESS, false);
	}

	public SM_ITEM_USAGE_ANIMATION(int playerObjId, int itemObjId, int itemId, int castTime, ItemUseAnimation animation) {
		this(playerObjId, playerObjId, itemObjId, itemId, castTime, animation, false);
	}

	public SM_ITEM_USAGE_ANIMATION(int playerObjId, int targetObjId, int itemObjId, int itemId, int castTime, ItemUseAnimation animation) {
		this(playerObjId, targetObjId, itemObjId, itemId, castTime, animation, false);
	}

	/**
	 * @param castTime
	 *          cast duration in milliseconds, only carried by the START stages
	 * @param suppressAnimation
	 *          true to make the client skip the animation and only report the use, which is what the pet does when it consumes an item on its own. Only
	 *          the four USE stages honour it, the soul bind and identify stages animate either way, so pass false unless a use has no visible actor.
	 */
	public SM_ITEM_USAGE_ANIMATION(int playerObjId, int targetObjId, int itemObjId, int itemId, int castTime, ItemUseAnimation animation,
		boolean suppressAnimation) {
		this.playerObjId = playerObjId;
		this.targetObjId = targetObjId;
		this.itemObjId = itemObjId;
		this.itemId = itemId;
		this.castTime = castTime;
		this.animation = animation;
		this.suppressAnimation = suppressAnimation;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeD(targetObjId);
		writeD(itemObjId);
		writeD(itemId);
		writeD(castTime);
		writeC(animation.getId());
		writeC(suppressAnimation ? 1 : 0);
		writeH(1); // number of trailing elements
		writeD(0); // the one element, which the client counts but never reads
	}
}
