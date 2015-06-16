package com.aionemu.gameserver.model.gameobjects;

import java.util.concurrent.atomic.AtomicReference;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HousingPostbox;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_OBJECT_USE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class PostboxObject extends HouseObject<HousingPostbox> {

	private AtomicReference<Player> usingPlayer = new AtomicReference<Player>();

	public PostboxObject(House owner, int objId, int templateId) {
		super(owner, objId, templateId);
	}

	@Override
	public void onUse(final Player player) {
		if (!usingPlayer.compareAndSet(null, player)) {
			// The same player is using, return. It might be double-click
			if (usingPlayer.compareAndSet(player, player))
				return;
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_OCCUPIED_BY_OTHER);
			return;
		}

		final ItemUseObserver observer = new ItemUseObserver() {
			@Override
			public void abort() {
				player.getObserveController().removeObserver(this);
				usingPlayer.set(null);
			}

		};

		player.getObserveController().attach(observer);

		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_USE(getObjectTemplate().getNameId()));
		player.getController().addTask(TaskId.HOUSE_OBJECT_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				try {
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.MAIL.id()));
					//player.getMailbox().sendMailList(false);
					PacketSendUtility.sendPacket(player, new SM_OBJECT_USE_UPDATE(player.getObjectId(), 0, 0, PostboxObject.this));
				}
				finally {
					player.getObserveController().removeObserver(observer);
					usingPlayer.set(null);
				}
			}

		}, 0));
	}

	@Override
	public boolean canExpireNow() {
		return usingPlayer.get() == null;
	}

}
