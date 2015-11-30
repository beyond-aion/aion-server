package com.aionemu.gameserver.model.templates.survey;

/**
 * @author Nathan
 */
public class CustomSurveyItem {

	private int itemId;
	private int itemCount;

	public CustomSurveyItem(int itemId, int itemCount) {
		this.itemId = itemId;
		this.itemCount = itemCount;
	}
	
	public int getId() {
		return itemId;
	}
	
	public int getCount() {
		return itemCount;
	}
}
