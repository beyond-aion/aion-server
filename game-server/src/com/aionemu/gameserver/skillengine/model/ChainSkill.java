package com.aionemu.gameserver.skillengine.model;

/**
 * @author kecimis
 * @modified Neon
 */
public class ChainSkill {

	private String category;
	private int useCount;
	private long lastUseTime;
	private long expireTime;

	public ChainSkill(String category, int useCount, long expireTime) {
		this.category = category;
		this.useCount = useCount;
		this.lastUseTime = useCount == 0 ? 0 : System.currentTimeMillis();
		this.expireTime = expireTime;
	}

	public void clear() {
		this.category = "";
		this.useCount = 0;
		this.lastUseTime = 0;
		this.expireTime = 0;
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

	/**
	 * @return The time when this chain skill can not be activated anymore, 0 if none is set.
	 */
	public long getExpireTime() {
		return expireTime;
	}

	/**
	 * @param expireTime
	 *          - the time when this chain skill can not be activated anymore (0 means no expiration).
	 */
	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}
}
