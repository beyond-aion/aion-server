package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonHouseObjectAction")
public class SummonHouseObjectAction extends AbstractItemAction {

	@XmlAttribute(name = "id")
	private int objectId;

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
		return objectId;
	}

}
