package com.aionemu.gameserver.model.gameobjects.player.emotion;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.EmotionLearnAction;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SVDNESS
 */

public record Emotion(int id, int expireTime) implements Expirable {
	private static volatile Map<Integer, Integer> itemNameIdByEmotionId;

	@Override
	public void onExpire(Player player) {
		player.getEmotions().remove(id);
		int nameId = getItemNameId(id);
		String emotionName = nameId != 0 ? ChatUtil.l10n(nameId) : "";
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DELETE_CASH_SOCIALACTION_BY_TIMEOUT(emotionName));
	}

	private static int getItemNameId(int emotionId) {
		Map<Integer, Integer> map = itemNameIdByEmotionId;
		if (map == null) {
			synchronized (Emotion.class) {
				map = itemNameIdByEmotionId;
				if (map == null) {
					map = buildItemNameIdMap();
					itemNameIdByEmotionId = map;
				}
			}
		}
		Integer nameId = map.get(emotionId);
		return nameId == null ? 0 : nameId;
	}

	private static Map<Integer, Integer> buildItemNameIdMap() {
		Map<Integer, Integer> map = new HashMap<>();
		for (ItemTemplate template : DataManager.ITEM_DATA.getItemTemplates()) {
			ItemActions actions = template.getActions();
			if (actions == null) {
				continue;
			}
			for (AbstractItemAction action : actions.getItemActions()) {
				if (action instanceof EmotionLearnAction emotion) {
					map.putIfAbsent(emotion.getEmotionId(), template.getL10nId());
				}
			}
		}
		return map;
	}
}