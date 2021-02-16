package com.aionemu.gameserver.model.items;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class ChargeInfo extends ActionObserver {

	public static final int LEVEL2 = 1000000;
	public static final int LEVEL1 = 500000;
	private final int attackBurn;
	private final int defendBurn;
	private final Item item;
	private int chargePoints;
	private Player player;

	public ChargeInfo(int chargePoints, Item item) {
		super(ObserverType.ATTACK_DEFEND);
		this.chargePoints = chargePoints;
		this.item = item;
		if (item.getImprovement() != null) {
			this.attackBurn = item.getImprovement().getBurnAttack();
			this.defendBurn = item.getImprovement().getBurnDefend();
		} else {
			this.attackBurn = 0;
			this.defendBurn = 0;
		}
	}

	public int getChargePoints() {
		return chargePoints;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Updates the chargePoints of the item.
	 * 
	 * @param pointsToAdd
	 *          chargePoints to add to the current charge points
	 * @return boolean indicating whether the visual charge bar has changed or not
	 */
	public synchronized boolean updateChargePoints(int pointsToAdd) {
		int newChargePoints = chargePoints + pointsToAdd;
		newChargePoints = Math.max(0, Math.min(newChargePoints, LEVEL2));
		int currentChargeBarStep = chargePoints / 50000;
		int newChargeBarStep = newChargePoints / 50000;
		chargePoints = newChargePoints;
		if (item.isEquipped() && player != null)
			player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
		item.setPersistentState(PersistentState.UPDATE_REQUIRED);
		return currentChargeBarStep != newChargeBarStep;
	}

	@Override
	public void attacked(Creature creature, int skillId) {
		if (updateChargePoints(-defendBurn))
			sendItemUpdate();
	}

	@Override
	public void attack(Creature creature, int skillId) {
		if (updateChargePoints(-attackBurn))
			sendItemUpdate();
	}

	private void sendItemUpdate() {
		if (player != null)
			PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item, ItemPacketService.ItemUpdateType.CHARGE));
	}

}
