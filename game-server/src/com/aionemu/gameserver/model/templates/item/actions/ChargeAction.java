package com.aionemu.gameserver.model.templates.item.actions;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.item.ItemChargeService;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChargeItemAction")
public class ChargeAction extends AbstractItemAction {

	@XmlAttribute
	protected int capacity;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		Collection<Item> conditioningItems = ItemChargeService.filterItemsToCondition(player, null, parentItem.getImprovement().getChargeWay());
		return conditioningItems.size() > 0;
	}

	@Override
	public void act(final Player player, Item parentItem, Item targetItem, Object... params) {
		if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1))
			return;
		Collection<Item> conditioningItems = ItemChargeService.filterItemsToCondition(player, null, parentItem.getImprovement().getChargeWay());
		ItemChargeService.chargeItems(player, conditioningItems, capacity);
	}

}
