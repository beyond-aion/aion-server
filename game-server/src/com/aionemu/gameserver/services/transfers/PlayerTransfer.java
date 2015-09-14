package com.aionemu.gameserver.services.transfers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author xTz
 */
public class PlayerTransfer {

	private byte[] commonData, itemsData, data, recipeData, skillData, questData;
	private final int taskId, targetAccount;
	private final String name, account;

	public PlayerTransfer(int taskId, int targetAccount, String account, String name) {
		this.taskId = taskId;
		this.targetAccount = targetAccount;
		this.account = account;
		this.name = name;
	}

	public String getAccount() {
		return account;
	}

	public int getTargetAccount() {
		return targetAccount;
	}

	public String getName() {
		return name;
	}

	public int getTaskId() {
		return taskId;
	}

	public byte[] getCommonData() {
		return commonData;
	}

	public byte[] getItemsData() {
		return itemsData;
	}

	public void setItemsData(byte[] itemsData) {
		this.itemsData = itemsData;
	}

	public void setCommonData(byte[] commonData) {
		this.commonData = commonData;
	}

	public byte[] getSkillData() {
		return skillData;
	}

	public byte[] getRecipeData() {
		return recipeData;
	}

	public byte[] getQuestData() {
		return questData;
	}

	public byte[] getData() {
		return data;
	}

	public void setSkillData(byte[] skillData) {
		this.skillData = skillData;
	}

	public void setRecipeData(byte[] recipeData) {
		this.recipeData = recipeData;
	}

	public void setQuestData(byte[] questData) {
		this.questData = questData;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public ByteBuffer getDB() {
		ByteBuffer buffer = ByteBuffer.allocate(getCommonData().length + getItemsData().length + getData().length + getSkillData().length
			+ getRecipeData().length + getQuestData().length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(getCommonData());
		buffer.put(getItemsData());
		buffer.put(getData());
		buffer.put(getSkillData());
		buffer.put(getRecipeData());
		buffer.put(getQuestData());
		buffer.flip();
		return buffer;
	}

}
