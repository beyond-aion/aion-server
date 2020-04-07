package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.services.RecipeService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, MrPoke, KID
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CraftLearnAction")
public class CraftLearnAction extends AbstractItemAction {

	@XmlAttribute
	protected int recipeid;

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		player.getController().cancelUseItem();
		if (player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
			if (RecipeService.addRecipe(player, recipeid, false)) {
				PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate()
					.getTemplateId()));
			}
		}
	}

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		return RecipeService.validateNewRecipe(player, recipeid) != null;
	}

	public int getRecipeId() {
		return recipeid;
	}
}
