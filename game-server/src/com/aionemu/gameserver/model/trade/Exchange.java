package com.aionemu.gameserver.model.trade;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public class Exchange {

	private Player activeplayer;
	private Player targetPlayer;

	private boolean confirmed;
	private boolean locked;

	private long kinahCount;

	private Map<Integer, ExchangeItem> items = new HashMap<>();

	private final Object monitor;

	/**
	 * @param monitor
	 *          The object both sides of the exchange synchronize on, the same for the exchange of the partner.
	 */
	public Exchange(Player activeplayer, Player targetPlayer, Object monitor) {
		this.activeplayer = activeplayer;
		this.targetPlayer = targetPlayer;
		this.monitor = monitor;
	}

	/**
	 * @return The object that guards the state of this exchange and of the partner's exchange.
	 */
	public Object getMonitor() {
		return monitor;
	}

	public void confirm() {
		confirmed = true;
	}

	/**
	 * @return the confirmed
	 */
	public boolean isConfirmed() {
		return confirmed;
	}

	public void lock() {
		this.locked = true;
	}

	/**
	 * @return the locked
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * @param exchangeItem
	 */
	public void addItem(int parentItemObjId, ExchangeItem exchangeItem) {
		this.items.put(parentItemObjId, exchangeItem);
	}

	/**
	 * @param countToAdd
	 */
	public void addKinah(long countToAdd) {
		this.kinahCount += countToAdd;
	}

	/**
	 * @return the activeplayer
	 */
	public Player getActiveplayer() {
		return activeplayer;
	}

	/**
	 * @return the targetPlayer
	 */
	public Player getTargetPlayer() {
		return targetPlayer;
	}

	/**
	 * @return the kinahCount
	 */
	public long getKinahCount() {
		return kinahCount;
	}

	/**
	 * @return the items
	 */
	public Map<Integer, ExchangeItem> getItems() {
		return items;
	}

	public boolean isExchangeListFull() {
		return items.size() >= 18;
	}

}
