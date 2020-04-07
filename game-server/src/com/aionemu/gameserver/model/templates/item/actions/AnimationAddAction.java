package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.motion.Motion;
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

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (parentItem == null) { // no item selected.
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR());
			return false;
		}

		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		player.getController().cancelUseItem();
		PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate()
			.getTemplateId(), 1000, 0, 0));
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (player.getInventory().decreaseItemCount(parentItem, 1) != 0)
					return;
				if (idle != null) {
					addMotion(player, idle);
				}
				if (run != null) {
					addMotion(player, run);
				}
				if (jump != null) {
					addMotion(player, jump);
				}
				if (rest != null) {
					addMotion(player, rest);
				}
				if (shop != null) {
					addMotion(player, shop);
				}
				PacketSendUtility.broadcastPacketAndReceive(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 0));
				PacketSendUtility.broadcastPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()), false);
			}
		}, 1000));
	}

	private void addMotion(Player player, int motionId) {
		Motion motion = new Motion(motionId, minutes == null ? 0 : (int) (System.currentTimeMillis() / 1000) + minutes * 60, true);
		player.getMotions().add(motion, true);
		PacketSendUtility.sendPacket(player, new SM_MOTION((short) motion.getId(), motion.secondsUntilExpiration()));
	}
}
