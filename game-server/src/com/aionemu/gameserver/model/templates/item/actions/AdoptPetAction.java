package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Rolandas
 */
public class AdoptPetAction extends AbstractItemAction {

	@XmlAttribute(name = "petId")
	private int petId;

	@XmlAttribute(name = "minutes")
	private int expireMinutes;

	@XmlAttribute(name = "sidekick")
	private Boolean isSideKick = false;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		return false;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
	}

	public int getPetId() {
		return petId;
	}

	public int getExpireMinutes() {
		return expireMinutes;
	}

	public Boolean isSideKick() {
		return isSideKick;
	}

}
