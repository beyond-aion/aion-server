package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractItemAction")
public abstract class AbstractItemAction {

	/**
	 * Check if an item can be used.
	 * 
	 * @param player
	 * @param parentItem
	 * @param targetItem
	 * @param params Optional parameters (implementation specific), same as in the following {@link #act(Player, Item, Item, Object...)} call.
	 * @return True if act() can be called
	 */
	public abstract boolean canAct(Player player, Item parentItem, Item targetItem, Object... params);

	/**
	 * @param player
	 * @param parentItem
	 * @param targetItem
	 * @param params Optional parameters (implementation specific), same as in previous {@link #canAct(Player, Item, Item, Object...)} call.
	 */
	public abstract void act(Player player, Item parentItem, Item targetItem, Object... params);

}
