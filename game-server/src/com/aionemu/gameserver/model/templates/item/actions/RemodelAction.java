package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Rolandas
 */
public class RemodelAction extends AbstractItemAction {

	@XmlAttribute(name = "type")
	private int extractType;

	@XmlAttribute(name = "minutes")
	private int expireMinutes;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		return false;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
	}

	public int getExpireMinutes() {
		return expireMinutes;
	}

	public int getExtractType() {
		return extractType;
	}

}
