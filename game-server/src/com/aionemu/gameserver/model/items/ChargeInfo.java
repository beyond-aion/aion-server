package com.aionemu.gameserver.model.items;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class ChargeInfo extends ActionObserver {

	public static final int LEVEL2 = 1000000;
	public static final int LEVEL1 = 500000;

	private int chargePoints;
	private final int attackBurn;
	private final int defendBurn;
	private final Item item;
	private Player player;

	/**
	 * @param chargePoints
	 */
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
		return this.chargePoints;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int updateChargePoints(int addPoints) {
		int newChargePoints = chargePoints + addPoints;
		if (newChargePoints > LEVEL2) {
			newChargePoints = LEVEL2;
		} else if (newChargePoints < 0) {
			newChargePoints = 0;
		}
		if (item.isEquipped() && player != null)
			player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
		item.setPersistentState(PersistentState.UPDATE_REQUIRED);
		this.chargePoints = newChargePoints;
		return newChargePoints;
	}

	@Override
	public void attacked(Creature creature, int skillId) {
		updateChargePoints(-defendBurn);
		Player player = this.player;
		if (player != null) {
			PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item));
		}
	}

	@Override
	public void attack(Creature creature) {
		updateChargePoints(-attackBurn);
		Player player = this.player;
		if (player != null) {
			PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item));
		}
	}

}
