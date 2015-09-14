package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ReturnLocList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ginho1
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultiReturnAction")
public class MultiReturnAction extends AbstractItemAction {
	@XmlAttribute(name = "id")
	protected int id;

	@Override
	public boolean canAct(Player player, Item item, Item targetItem) {
	       return true;
	}

	@Override
	public void act(final Player player, final Item item, final Item targetItem) {
	
		final ItemUseObserver observer = new ItemUseObserver() {
			@Override
			public void abort() {
			       player.getController().cancelTask(TaskId.ITEM_USE);
			       player.removeItemCoolDown(item.getItemTemplate().getUseLimits().getDelayId());
			       PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402147, new DescriptionId(targetItem.getNameId())));
			       player.getObserveController().removeObserver(this);
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				 player.getObserveController().removeObserver(observer);

				 ReturnLocList loc = DataManager.MULTIRETURN_DATA.getReturnLocListById(id).get(item.getIndexReturn());
				 if(loc != null){
					    if(loc.getAlias() != null && loc.getWorldid() > 0){
						    TeleportService2.useTeleportScroll(player, loc.getAlias().toUpperCase(), loc.getWorldid());
						    PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(new DescriptionId(item.getNameId())));
						    player.getInventory().decreaseByObjectId(item.getObjectId(), 1);
					    }
				 }
			}

		}, 5000));
	}
}