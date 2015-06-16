package com.aionemu.gameserver.model.gameobjects;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.DateTime;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HousingUseableItem;
import com.aionemu.gameserver.model.templates.housing.LimitType;
import com.aionemu.gameserver.model.templates.housing.UseItemAction;
import com.aionemu.gameserver.network.PacketWriteHelper;
import com.aionemu.gameserver.network.aion.serverpackets.SM_OBJECT_USE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Rolandas
 */
public class UseableItemObject extends HouseObject<HousingUseableItem> {

	private volatile boolean mustGiveLastReward = false;
	private AtomicReference<Player> usingPlayer = new AtomicReference<Player>();
	private UseDataWriter entryWriter = null;

	public UseableItemObject(House owner, int objId, int templateId) {
		super(owner, objId, templateId);
		UseItemAction action = getObjectTemplate().getAction();
		if (action != null && action.getFinalRewardId() != null && getUseSecondsLeft() == 0)
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
			if (obj.getObjectTemplate().getUseCount() != null)
				writeD(buffer, obj.getOwnerUsedCount() + obj.getVisitorUsedCount());
			else
				writeD(buffer, 0);
			int checkType = 0;
			UseItemAction action = obj.getObjectTemplate().getAction();
			if (action != null && action.getCheckType() != null)
				checkType = action.getCheckType();
			writeC(buffer, checkType);
		}
	}

	@Override
	public void onUse(final Player player) {
		if (!usingPlayer.compareAndSet(null, player)) {
			// The same player is using, return. It might be double-click
			if (usingPlayer.compareAndSet(player, player))
				return;
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_OCCUPIED_BY_OTHER);
			return;
		}

		boolean isOwner = getOwnerHouse().getOwnerId() == player.getObjectId();
		if (getObjectTemplate().isOwnerOnly() && !isOwner) {
			warnAndRelease(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_IS_ONLY_FOR_OWNER_VALID);
			return;
		}

		boolean cdExpired = player.getHouseObjectCooldownList().isCanUseObject(getObjectId());
		if (!cdExpired) {
			if (getObjectTemplate().getCd() != null && getObjectTemplate().getCd() != 0) {
				warnAndRelease(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANNOT_USE_FLOWERPOT_COOLTIME);
				return;
			}
			if (getObjectTemplate().isOwnerOnly() && isOwner) {
				warnAndRelease(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_CANT_USE_PER_DAY);
				return;
			}
		}

		final UseItemAction action = getObjectTemplate().getAction();
		if (action == null) {
			// Some objects do not have actions; they are test items now
			warnAndRelease(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_ALL_CANT_USE);
			return;
		}

		if (getObjectTemplate().getPlacementLimit() == LimitType.COOKING) {
			// Check if player already has an item
			if (player.getInventory().getItemCountByItemId(action.getRewardId()) > 0) {
				int nameId = DataManager.ITEM_DATA.getItemTemplate(action.getRewardId()).getNameId();
				SM_SYSTEM_MESSAGE msg = SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_USE_ALREADY_HAVE_REWARD_ITEM(nameId, getObjectTemplate().getNameId());
				warnAndRelease(player, msg);
				return;
			}
		}

		final Integer useCount = getObjectTemplate().getUseCount();
		int currentUseCount = 0;

		if (useCount != null) {
			// Counter is for both, but could be made custom from configs
			currentUseCount = getOwnerUsedCount() + getVisitorUsedCount();
			if (currentUseCount >= useCount && !isOwner || currentUseCount > useCount && isOwner) {
				// if expiration is set then final reward has to be given for owner only
				// due to inventory full. If inventory was not full, the object had to be despawned, so
				// we wouldn't reach this check.
				if (!mustGiveLastReward || !isOwner) {
					warnAndRelease(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT);
					return;
				}
			}
		}

		final Integer requiredItem = getObjectTemplate().getRequiredItem();

		if (mustGiveLastReward && !isOwner) {
			// Expired, waiting owner
			int descId = DataManager.ITEM_DATA.getItemTemplate(requiredItem).getNameId();
			warnAndRelease(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_DELETE_EXPIRE_TIME(descId));
			return;
		}
		else if (requiredItem != null) {
			int checkType = action.getCheckType();
			if (checkType == 1) { // equip item needed
				List<Item> items = player.getEquipment().getEquippedItemsByItemId(requiredItem);
				if (items.size() == 0) {
					int descId = DataManager.ITEM_DATA.getItemTemplate(requiredItem).getNameId();
					warnAndRelease(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_USE_HOUSE_OBJECT_ITEM_EQUIP(new DescriptionId(descId)));
					return;
				}
			}
			else if (player.getInventory().getItemCountByItemId(requiredItem) < action.getRemoveCount()) {
				int descId = DataManager.ITEM_DATA.getItemTemplate(requiredItem).getNameId();
				warnAndRelease(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_USE_HOUSE_OBJECT_ITEM_CHECK(new DescriptionId(descId)));
				return;
			}
		}

		if (player.getInventory().isFull()) {
			warnAndRelease(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_TOO_MANY_ITEMS_INVENTORY);
			return;
		}

		final int delay = getObjectTemplate().getDelay();
		final int ownerId = getOwnerHouse().getOwnerId();
		final int usedCount = useCount == null ? 0 : currentUseCount + 1;
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, 9));
				player.getObserveController().removeObserver(this);
				warnAndRelease(player, null);
			}

			@Override
			public void itemused(Item item) {
				abort();
			}
		};

		player.getObserveController().attach(observer);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_USE(getObjectTemplate().getNameId()));
		PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 8));
		player.getController().addTask(TaskId.HOUSE_OBJECT_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				try {
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, 9));
					if (action.getRemoveCount() != null && action.getRemoveCount() != 0) {
						if (!player.getInventory().decreaseByItemId(requiredItem, action.getRemoveCount())) {
							AuditLogger.info(player, "Player try use useble object without required item");
							return;
						}
					}

					UseableItemObject myself = UseableItemObject.this;
					int rewardId = 0;
					boolean delete = false;

					if (useCount != null) {
						if (action.getFinalRewardId() != null && useCount + 1 == usedCount) {
							// visitors do not get final rewards
							rewardId = action.getFinalRewardId();
							ItemService.addItem(player, rewardId, 1);
							delete = true;
						}
						else if (action.getRewardId() != null) {
							rewardId = action.getRewardId();
							ItemService.addItem(player, rewardId, 1);
							if (useCount == usedCount) {
								PacketSendUtility.sendPacket(player,
									SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_FLOWERPOT_GOAL(myself.getObjectTemplate().getNameId()));
								if (action.getFinalRewardId() == null) {
									delete = true;
								}
								else {
									myself.setMustGiveLastReward(true);
									myself.setExpireTime((int) (System.currentTimeMillis() / 1000));
									myself.setPersistentState(PersistentState.UPDATE_REQUIRED);
								}
							}
						}
					}
					else if (action.getRewardId() != null) {
						rewardId = action.getRewardId();
						ItemService.addItem(player, rewardId, 1);
					}
					if (usedCount > 0) {
						if (!delete)
							if (player.getObjectId() == ownerId)
								myself.incrementOwnerUsedCount();
							else
								myself.incrementVisitorUsedCount();
						PacketSendUtility.broadcastPacket(player, new SM_OBJECT_USE_UPDATE(player.getObjectId(), ownerId, usedCount, myself), true);
					}
					if (rewardId > 0) {
						int rewardNameId = DataManager.ITEM_DATA.getItemTemplate(rewardId).getNameId();
						PacketSendUtility.sendPacket(player,
							SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_REWARD_ITEM(myself.getObjectTemplate().getNameId(), rewardNameId));
					}
					if (delete)
						selfDestroy(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_DELETE_USE_COUNT_FINAL(getObjectTemplate().getNameId()));
					else {
						Integer cd = myself.getObjectTemplate().getCd();
						DateTime repeatDate;
						if (cd == null || cd == 0) {
							// use once per day
							DateTime tomorrow = DateTime.now().plusDays(1);
							repeatDate = new DateTime(tomorrow.getYear(), tomorrow.getMonthOfYear(), tomorrow.getDayOfMonth(), 0, 0, 0);
							cd = (int) (repeatDate.getMillis() - DateTime.now().getMillis()) / 1000;
						}
						player.getHouseObjectCooldownList().addHouseObjectCooldown(myself.getObjectId(), cd);
					}
				}
				finally {
					player.getObserveController().removeObserver(observer);
					warnAndRelease(player, null);
				}
			}
		}, delay));
	}

	private void warnAndRelease(Player player, SM_SYSTEM_MESSAGE systemMessage) {
		if (systemMessage != null)
			PacketSendUtility.sendPacket(player, systemMessage);
		usingPlayer.set(null);
	}

	public void setMustGiveLastReward(boolean mustGiveLastReward) {
		this.mustGiveLastReward = mustGiveLastReward;
	}

	@Override
	public boolean canExpireNow() {
		if (mustGiveLastReward)
			return false;
		return usingPlayer.get() == null;
	}

	public void writeUsageData(ByteBuffer buffer) {
		entryWriter.writeMe(buffer);
	}
}
