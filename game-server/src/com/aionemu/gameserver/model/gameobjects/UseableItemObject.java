package com.aionemu.gameserver.model.gameobjects;

import java.nio.ByteBuffer;
import java.time.LocalTime;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.HousingUseableItem;
import com.aionemu.gameserver.model.templates.housing.LimitType;
import com.aionemu.gameserver.model.templates.housing.UseItemAction;
import com.aionemu.gameserver.network.PacketWriteHelper;
import com.aionemu.gameserver.network.aion.serverpackets.SM_OBJECT_USE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Rolandas, Neon
 */
public class UseableItemObject extends UseableHouseObject<HousingUseableItem> {

	private volatile boolean mustGiveLastReward = false;
	private final UseDataWriter entryWriter;

	public UseableItemObject(HouseRegistry registry, int objId, int templateId) {
		super(registry, objId, templateId);
		UseItemAction action = getObjectTemplate().getAction();
		if (action != null && action.getFinalRewardId() != null && isExpired())
			mustGiveLastReward = true;
		entryWriter = new UseDataWriter(this);
	}

	static class UseDataWriter extends PacketWriteHelper {

		UseableItemObject obj;

		public UseDataWriter(UseableItemObject obj) {
			this.obj = obj;
		}

		@Override
		protected void writeMe(ByteBuffer buffer) {
			writeD(buffer, obj.getObjectTemplate().getUseCount() == null ? 0 : obj.getOwnerUsedCount() + obj.getVisitorUsedCount());
			UseItemAction action = obj.getObjectTemplate().getAction();
			writeC(buffer, action == null || action.getCheckType() == null ? 0 : action.getCheckType());
		}
	}

	@Override
	public void onUse(final Player player) {
		UseItemAction action = getObjectTemplate().getAction();
		if (action == null) { // Some objects do not have actions; they are test items now
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_ALL_CANT_USE());
			return;
		}

		int ownerId = getOwnerHouse().getOwnerId();
		boolean isOwner = ownerId == player.getObjectId();
		if (!isOwner && getObjectTemplate().isOwnerOnly()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_IS_ONLY_FOR_OWNER_VALID());
			return;
		}

		if (player.getHouseObjectCooldowns().hasCooldown(getObjectId())) {
			if (getObjectTemplate().getCd() != null && getObjectTemplate().getCd() > 0)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANNOT_USE_FLOWERPOT_COOLTIME());
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_CANT_USE_PER_DAY());
			return;
		}

		final Integer useCount = getObjectTemplate().getUseCount();
		int currentUseCount = 0;
		if (useCount != null) {
			// Counter is for both, but could be made custom from configs
			currentUseCount = getOwnerUsedCount() + getVisitorUsedCount();
			if (currentUseCount >= useCount && !isOwner || currentUseCount > useCount && isOwner) {
				// if expiration is set then final reward has to be given for owner only due to inventory full. If inventory was not full, the object had to
				// be despawned, so we wouldn't reach this check.
				if (!mustGiveLastReward || !isOwner) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT());
					return;
				}
			}
		}

		if (mustGiveLastReward && !isOwner) { // expired, wait for owner
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_DELETE_EXPIRE_TIME(getObjectTemplate().getL10n()));
			return;
		}

		if (getObjectTemplate().getPlacementLimit() == LimitType.COOKING) {
			// Check if player already has an item
			if (player.getInventory().getItemCountByItemId(action.getRewardId()) > 0) {
				String rewardL10n = DataManager.ITEM_DATA.getItemTemplate(action.getRewardId()).getL10n();
				PacketSendUtility.sendPacket(player,
					SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_USE_ALREADY_HAVE_REWARD_ITEM(rewardL10n, getObjectTemplate().getL10n()));
				return;
			}
		}

		final Integer requiredItem = getObjectTemplate().getRequiredItem();
		if (requiredItem != null) {
			if (action.getCheckType() == 1) { // equip item needed
				if (player.getEquipment().getEquippedItemsByItemId(requiredItem).size() == 0) {
					String requiredItemL10n = DataManager.ITEM_DATA.getItemTemplate(requiredItem).getL10n();
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_USE_HOUSE_OBJECT_ITEM_EQUIP(requiredItemL10n));
					return;
				}
			} else if (player.getInventory().getItemCountByItemId(requiredItem) < action.getRemoveCount()) {
				String requiredItemL10n = DataManager.ITEM_DATA.getItemTemplate(requiredItem).getL10n();
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_USE_HOUSE_OBJECT_ITEM_CHECK(requiredItemL10n));
				return;
			}
		}

		if (requiredItem != null ^ action.getRemoveCount() != null) {
			LoggerFactory.getLogger(UseableItemObject.class)
				.warn(this + " doesn't have valid usage requirements " + (requiredItem == null ? " (item missing)" : "(remove count missing)"));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_ALL_CANT_USE());
			return;
		}

		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_TOO_MANY_ITEMS_INVENTORY());
			return;
		}

		if (!setOccupant(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_OCCUPIED_BY_OTHER());
			return;
		}

		final int delayMs = getObjectTemplate().getDelay();
		final int usedCount = useCount == null ? 0 : currentUseCount + 1;
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_USE(getObjectTemplate().getL10n()));
		PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delayMs, 8));
		player.getController().addTask(TaskId.HOUSE_OBJECT_USE, ThreadPoolManager.getInstance().schedule(() -> {
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, 9));
			if (requiredItem != null && action.getRemoveCount() != null && action.getRemoveCount() > 0) {
				if (!player.getInventory().decreaseByItemId(requiredItem, action.getRemoveCount()))
					return;
			}

			int rewardId = 0;
			boolean delete = false;

			if (useCount != null) {
				if (action.getFinalRewardId() != null && useCount + 1 == usedCount) {
					// visitors do not get final rewards
					rewardId = action.getFinalRewardId();
					delete = true;
				} else if (action.getRewardId() != null) {
					rewardId = action.getRewardId();
					if (useCount == usedCount) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_FLOWERPOT_GOAL(getObjectTemplate().getL10n()));
						if (action.getFinalRewardId() == null) {
							delete = true;
						} else {
							setMustGiveLastReward(true);
							setExpireTime((int) (System.currentTimeMillis() / 1000));
							setPersistentState(PersistentState.UPDATE_REQUIRED);
						}
					}
				}
			} else if (action.getRewardId() != null) {
				rewardId = action.getRewardId();
			}
			if (usedCount > 0) {
				if (!delete)
					if (isOwner)
						incrementOwnerUsedCount();
					else
						incrementVisitorUsedCount();
			}
			if (rewardId > 0) {
				ItemService.addItem(player, rewardId, 1);
				String rewardL10n = DataManager.ITEM_DATA.getItemTemplate(rewardId).getL10n();
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_REWARD_ITEM(getObjectTemplate().getL10n(), rewardL10n));
			}
			PacketSendUtility.broadcastPacket(player, new SM_OBJECT_USE_UPDATE(player.getObjectId(), ownerId, usedCount, UseableItemObject.this), true);
			if (delete)
				despawnAndRemoveHouseObject(player, false);
			else {
				long reuseTime;
				Integer cd = getObjectTemplate().getCd();
				if (cd == null || cd == 0) // use once per day (cooldown ends at midnight)
					reuseTime = ServerTime.now().with(LocalTime.MAX).toEpochSecond() * 1000;
				else
					reuseTime = System.currentTimeMillis() + cd * 1000;
				player.getHouseObjectCooldowns().put(getObjectId(), reuseTime);
			}
		}, delayMs));
	}

	public void setMustGiveLastReward(boolean mustGiveLastReward) {
		this.mustGiveLastReward = mustGiveLastReward;
	}

	@Override
	public boolean canExpireNow() {
		return !mustGiveLastReward && !isOccupied();
	}

	public void writeUsageData(ByteBuffer buffer) {
		entryWriter.writeMe(buffer);
	}

	@Override
	public boolean hasUseCooldown() {
		return getObjectTemplate().getCd() != null && getObjectTemplate().getCd() > 0;
	}

}
