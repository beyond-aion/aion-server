package com.aionemu.gameserver.model.gameobjects.player.motion;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.AnimationAddAction;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, SVDNESS
 */

public class Motion implements Expirable {
	static final Map<Integer, Integer> motionType = new LinkedHashMap<>();
	private static volatile Map<Integer, Integer> itemNameIdByMotionId;

	static {
		motionType.put(1, 1);
		motionType.put(2, 2);
		motionType.put(3, 3);
		motionType.put(4, 4);
		motionType.put(5, 1);
		motionType.put(6, 2);
		motionType.put(7, 3);
		motionType.put(8, 4);
		motionType.put(9, 5);
		motionType.put(10, 5);
		motionType.put(11, 1);
		motionType.put(12, 2);
		motionType.put(13, 3);
		motionType.put(14, 4);
		motionType.put(15, 1);
		motionType.put(16, 2);
		motionType.put(17, 3);
		motionType.put(18, 4);
		motionType.put(19, 5);
		motionType.put(20, 1);
		motionType.put(21, 1);
		motionType.put(22, 1);
		motionType.put(23, 1);
		motionType.put(24, 2);
		motionType.put(25, 4);
		motionType.put(26, 3);
	}

	private final int id;
	private final int deletionTime;
	private boolean active;
	
	public Motion(int id, int deletionTime, boolean isActive) {
		this.id = id;
		this.deletionTime = deletionTime;
		this.active = isActive;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public int expireTime() {
		return deletionTime;
	}

	@Override
	public void onExpire(Player player) {
		player.getMotions().remove(id);
		int nameId = getItemNameId(id);
		String motionName = nameId != 0 ? ChatUtil.l10n(nameId) : "";
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DELETE_CASH_CUSTOMANIMATION_BY_TIMEOUT(motionName));
	}

	private static int getItemNameId(int motionId) {
		Map<Integer, Integer> map = itemNameIdByMotionId;
		if (map == null) {
			synchronized (Motion.class) {
				map = itemNameIdByMotionId;
				if (map == null) {
					map = buildItemNameIdMap();
					itemNameIdByMotionId = map;
				}
			}
		}
		Integer nameId = map.get(motionId);
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
				if (action instanceof AnimationAddAction anim) {
					for (int mId : anim.getMotionIds()) {
						map.putIfAbsent(mId, template.getL10nId());
					}
				}
			}
		}
		return map;
	}
}