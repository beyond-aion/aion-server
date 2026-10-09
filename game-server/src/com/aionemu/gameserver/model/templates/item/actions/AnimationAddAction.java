package com.aionemu.gameserver.model.templates.item.actions;

import static com.aionemu.gameserver.model.items.ItemUseAnimation.*;

import java.util.EnumMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.motion.Motion;
import com.aionemu.gameserver.model.gameobjects.player.motion.MotionType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnimationAddAction")
public class AnimationAddAction extends AbstractItemAction {

	@XmlAttribute
	protected Integer idle;
	@XmlAttribute
	protected Integer run;
	@XmlAttribute
	protected Integer jump;
	@XmlAttribute
	protected Integer rest;
	@XmlAttribute
	protected Integer shop;
	@XmlAttribute
	protected Integer minutes;

	/**
	 * @return The animations this item teaches, by the slot each one replaces.
	 */
	public Map<MotionType, Integer> getMotionIds() {
		Map<MotionType, Integer> ids = new EnumMap<>(MotionType.class);
		if (idle != null)
			ids.put(MotionType.IDLE, idle);
		if (run != null)
			ids.put(MotionType.RUN, run);
		if (jump != null)
			ids.put(MotionType.JUMP, jump);
		if (rest != null)
			ids.put(MotionType.REST, rest);
		if (shop != null)
			ids.put(MotionType.SHOP, shop);
		return ids;
	}

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (parentItem == null) { // no item selected.
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR());
			return false;
		}
		if (hasAnyMotion(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CUSTOMANIMATION_ALREADY_HAS_MOTION());
			return false;
		}
		return true;
	}

	/**
	 * An animation can't be learned again while the player still has it, no matter if it's permanent or temporary.
	 */
	private boolean hasAnyMotion(Player player) {
		for (int motionId : getMotionIds().values()) {
			if (player.getMotions().getMotions().containsKey(motionId))
				return true;
		}
		return false;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		int castingDelay = parentItem.getItemTemplate().getCastingDelay();
		if (castingDelay <= 0) {
			finishUse(player, parentItem);
			return;
		}
		ItemUseObserver observer = new ItemUseObserver(player) {

			@Override
			protected void onAbort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.sendPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, USE_CANCEL));
			}

		};

		player.getObserveController().addObserver(observer);
		PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(),
			parentItem.getItemTemplate().getTemplateId(), castingDelay, USE_START));
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			finishUse(player, parentItem);
		}, castingDelay));
	}

	private void finishUse(Player player, Item parentItem) {
		if (hasAnyMotion(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_IS_NOT_USABLE());
			PacketSendUtility.broadcastPacketAndReceive(player,
				new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, USE_FAIL));
			return;
		}
		if (player.getInventory().decreaseItemCount(parentItem, 1) != 0)
			return;
		player.startCooldown(parentItem);
		for (int motionId : getMotionIds().values())
			addMotion(player, motionId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(parentItem.getL10n()));
		PacketSendUtility.broadcastPacketAndReceive(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, USE_SUCCESS));
		PacketSendUtility.broadcastPacket(player, SM_MOTION.playerMotions(player), false);
	}

	private void addMotion(Player player, int motionId) {
		Motion motion = new Motion(motionId, minutes == null ? 0 : (int) (System.currentTimeMillis() / 1000) + minutes * 60, true);
		player.getMotions().add(motion, true);
		PacketSendUtility.sendPacket(player, SM_MOTION.add(motion));
	}
}
