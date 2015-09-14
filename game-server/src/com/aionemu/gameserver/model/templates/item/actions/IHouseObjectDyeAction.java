package com.aionemu.gameserver.model.templates.item.actions;

import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Rolandas
 */
public interface IHouseObjectDyeAction {

	boolean canAct(Player player, Item parentItem, HouseObject<?> targetHouseObject);

	void act(Player player, Item parentItem, HouseObject<?> targetHouseObject);

}
