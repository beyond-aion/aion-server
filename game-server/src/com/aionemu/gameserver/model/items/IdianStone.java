package com.aionemu.gameserver.model.items;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class IdianStone extends ItemStone {

	private ActionObserver actionListener;
	private int polishCharge;
	private final Item item;
	private final int burnDefend;
	private final int burnAttack;
	private final RandomBonusEffect rndBonusEffect;

	public IdianStone(int itemId, PersistentState persistentState, Item item, int polishNumber, int polishCharge) {
		super(item.getObjectId(), itemId, 0, persistentState);
		this.item = item;
		burnDefend = item.getItemTemplate().getIdianAction().getBurnDefend();
		burnAttack = item.getItemTemplate().getIdianAction().getBurnAttack();
		this.polishCharge = polishCharge;
		ItemActions actions = DataManager.ITEM_DATA.getItemTemplate(itemId).getActions();
		rndBonusEffect = new RandomBonusEffect(StatBonusType.POLISH, actions.getPolishAction().getPolishSetId(), polishNumber);
	}

	public void onEquip(final Player player, long slot) {
		if (polishCharge > 0) {
			actionListener = new ActionObserver(ObserverType.ALL) {

				@Override
				public void attacked(Creature creature, int skillId) {
					decreasePolishCharge(player, true);
				}

				@Override
				public void attack(Creature creature) {
					decreasePolishCharge(player, false);
				}

			};
			player.getObserveController().addObserver(actionListener);
			if ((slot & ItemSlot.MAIN_HAND.getSlotIdMask()) != 0)
				rndBonusEffect.applyEffect(player);
		}
	}

	private synchronized void decreasePolishCharge(Player player, boolean isAttacked) {
		decreasePolishCharge(player, isAttacked, 0);
	}

	public synchronized void decreasePolishCharge(Player player, int skillValue) {
		decreasePolishCharge(player, false, skillValue);
	}

	private synchronized void decreasePolishCharge(Player player, boolean isAttacked, int skillValue) {
		int result;
		if (polishCharge <= 0) {
			return;
		}
		if (skillValue == 0)
			result = isAttacked ? burnDefend : burnAttack;
		else
			result = skillValue;
		if (polishCharge - result < 0) {
			polishCharge = 0;
		} else {
			polishCharge -= result;
		}
		if (polishCharge == 0) {
			onUnEquip(player);
			PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_POLISH_CHANGE_CONDITION_END(item.getL10n()));
			item.setIdianStone(null);
			setPersistentState(PersistentState.DELETED);
			DAOManager.getDAO(ItemStoneListDAO.class).storeIdianStones(this);
		}
	}

	public int getPolishNumber() {
		return rndBonusEffect.getStatBonusId();
	}

	public int getPolishCharge() {
		return polishCharge;
	}

	public void onUnEquip(Player player) {
		if (actionListener != null) {
			rndBonusEffect.endEffect(player);
			player.getObserveController().removeObserver(actionListener);
			actionListener = null;
		}
	}

}
