package com.aionemu.gameserver.skillengine.model;

/**
 * @author kecimis, Neon
 */
public class ChainSkill {

	private String category;
	private int useCount = 0;
	private long lastUseTime = 0;

	public ChainSkill(String category) {
		this.category = category;
	}

	public void clear() {
		this.category = "";
		this.useCount = 0;
		this.lastUseTime = 0;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String name) {
		this.category = name;
	}

	public int getUseCount() {
		return useCount;
	}

	public void increaseUseCount() {
		this.useCount++;
		this.lastUseTime = System.currentTimeMillis();
	}

	/**
	 * @return The time when this chain skill was last activated, 0 if never.
	 */
	public long getLastUseTime() {
		return lastUseTime;
	}
}
