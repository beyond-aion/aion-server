package com.aionemu.gameserver.model.templates.item.actions;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EmotionLearnAction")
public class EmotionLearnAction extends AbstractItemAction {

	private static final Set<Integer> LEARNABLE_IDS = ConcurrentHashMap.newKeySet();

	@XmlAttribute(name = "emotionid", required = true)
	private int emotionId;
	@XmlAttribute
	private int minutes;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		LEARNABLE_IDS.add(emotionId);
	}

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (emotionId == 0 || parentItem == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR());
			return false;
		}
		if (player.getEmotions() != null && player.getEmotions().contains(emotionId)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_TOOLTIP_LEARNED_EMOTION());
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		ItemTemplate itemTemplate = parentItem.getItemTemplate();
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), itemTemplate.getTemplateId()), true);

		player.getEmotions().add(emotionId, minutes == 0 ? 0 : (int) (System.currentTimeMillis() / 1000) + minutes * 60, true);
		player.getInventory().delete(parentItem);

	}

	/**
	 * Learnable IDs as of 4.8:<br>
	 * 64 - 155<br>
	 * <br>
	 * Not learnable known valid IDs:<br>
	 * 1 - 34 - default emotions<br>
	 * >10000 - housing emotions (10006/10007 lay in left/right side of a bed, 10008 sitting on a chair, ...)
	 * 
	 * @return True if there exists a learn template for given emotion. False means it's either a default or an invalid emotion.
	 */
	public static boolean isLearnable(int emotionId) {
		return LEARNABLE_IDS.contains(emotionId);
	}
}
