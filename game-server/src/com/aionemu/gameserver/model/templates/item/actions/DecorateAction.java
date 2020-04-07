package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Rolandas
 */
public class DecorateAction extends AbstractItemAction {

	@XmlAttribute(name = "id")
	private Integer partId;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		// TODO Auto-generated method stub

	}

	public int getTemplateId() {
		if (partId == null) // Addons missing in client
			return 0;
		return partId;
	}

}
