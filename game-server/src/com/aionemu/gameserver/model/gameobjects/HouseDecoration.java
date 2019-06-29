package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.housing.HousePart;

/**
 * @author Rolandas
 */
public class HouseDecoration extends AionObject implements Persistable {

	private final int templateId;
	private byte room;
	private PersistentState persistentState;

	public HouseDecoration(int objectId, int templateId) {
		this(objectId, templateId, -1);
	}

	public HouseDecoration(int objectId, int templateId, int room) {
		super(objectId);
		this.templateId = templateId;
		this.room = (byte) room;
		this.persistentState = PersistentState.NEW;
	}

	public int getTemplateId() {
		return templateId;
	}

	public HousePart getTemplate() {
		return DataManager.HOUSE_PARTS_DATA.getPartById(templateId);
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	public void setPersistentState(PersistentState persistentState) {
		this.persistentState = persistentState;
	}

	@Override
	public String getName() {
		return getTemplate().getName();
	}

	public byte getRoom() {
		return room;
	}

	public void setRoom(int value) {
		room = (byte) value;
	}
}
