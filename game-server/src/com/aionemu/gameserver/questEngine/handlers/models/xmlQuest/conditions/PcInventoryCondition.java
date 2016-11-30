package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.conditions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PcInventoryCondition")
public class PcInventoryCondition extends QuestCondition {

	@XmlAttribute(name = "item_id", required = true)
	protected int itemId;
	@XmlAttribute(required = true)
	protected long count;

	/**
	 * Gets the value of the itemId property.
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * Gets the value of the count property.
	 */
	public long getCount() {
		return count;
	}

	@Override
	public boolean doCheck(QuestEnv env) {
		Player player = env.getPlayer();
		long itemCount = player.getInventory().getItemCountByItemId(itemId);
		switch (getOp()) {
			case EQUAL:
				return itemCount == count;
			case GREATER:
				return itemCount > count;
			case GREATER_EQUAL:
				return itemCount >= count;
			case LESSER:
				return itemCount < count;
			case LESSER_EQUAL:
				return itemCount <= count;
			case NOT_EQUAL:
				return itemCount != count;
			default:
				return false;
		}
	}
}
