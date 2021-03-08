package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillLearnAction")
public class SkillLearnAction extends AbstractItemAction {

	@XmlAttribute
	protected int skillid;
	@XmlAttribute
	protected int level;
	@XmlAttribute(name = "class")
	protected PlayerClass playerClass;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		// 1. check player level
		if (player.getCommonData().getLevel() < level)
			return false;

		PlayerClass pc = player.getCommonData().getPlayerClass();
		if (!validateClass(pc))
			return false;

		// 4. check player race and Race.PC_ALL
		Race race = parentItem.getItemTemplate().getRace();
		if (player.getRace() != race && race != Race.PC_ALL)
			return false;
		// 5. check whether this skill is already learned
		if (player.getSkillList().isSkillPresent(skillid))
			return false;

		return true;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		// item animation and message
		ItemTemplate itemTemplate = parentItem.getItemTemplate();
		// PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.USE_ITEM(itemTemplate.getDescription()));
		player.getController().cancelUseItem();
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), itemTemplate.getTemplateId()), true);

		// add skill
		SkillLearnService.learnSkillBook(player, skillid);

		// remove book from inventory (assuming its not stackable)
		Item item = player.getInventory().getItemByObjId(parentItem.getObjectId());
		player.getInventory().delete(item);
	}

	private boolean validateClass(PlayerClass pc) {
		return playerClass == null || playerClass == pc || playerClass == pc.getStartingClass();
	}
}
