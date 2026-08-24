package com.aionemu.gameserver.skillengine.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemUseAction")
public class ItemUseAction extends Action {

	@XmlAttribute(required = true)
	protected int itemid;

	@XmlAttribute(required = true)
	protected int count;

	/** False, if the item is required but not consumed. */
	@XmlAttribute
	protected boolean expendable = true;

	@Override
	public boolean canAct(Skill skill) {
		if (!(skill.getEffector() instanceof Player player))
			return true;
		if (player.getInventory().getItemCountByItemId(itemid) >= count)
			return true;
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_NOT_ENOUGH_ITEM(DataManager.ITEM_DATA.getItemTemplate(itemid).getL10n()));
		return false;
	}

	@Override
	public boolean act(Skill skill) {
		if (!(skill.getEffector() instanceof Player player))
			return true;
		if (!expendable)
			return canAct(skill);
		if (!player.getInventory().decreaseByItemId(itemid, count)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_NOT_ENOUGH_ITEM(DataManager.ITEM_DATA.getItemTemplate(itemid).getL10n()));
			return false;
		}
		return true;
	}
}
