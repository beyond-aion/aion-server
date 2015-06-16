package com.aionemu.gameserver.model.broker;

/**
 * @author kosyachok
 */
public enum BrokerMessages {
	CANT_REGISTER_ITEM(2),
	NO_SPACE_AVAIABLE(3),
	NO_ENOUGHT_KINAH(5);

	private int id;

	private BrokerMessages(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
