package com.aionemu.gameserver.skillengine.model;

/**
 * @author kecimis
 */
public class ChainSkill {

	private String category;
	private int chainCount = 0;
	private long useTime;

	public ChainSkill(String category, int chainCount, long useTime) {
		this.category = category;
		this.chainCount = chainCount;
		this.useTime = useTime;
	}

	public void updateChainSkill(String category) {
		this.category = category;
		this.chainCount = 0;
		this.useTime = System.currentTimeMillis();
	}

	/**
	 * @return the name
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setCategory(String name) {
		this.category = name;
	}

	/**
	 * @return the chainCount
	 */
	public int getChainCount() {
		return chainCount;
	}

	/**
	 * @param chainCount
	 *          the chainCount to set
	 */
	public void setChainCount(int chainCount) {
		this.chainCount = chainCount;
	}

	public void increaseChainCount() {
		this.chainCount++;
	}

	/**
	 * @return the useTime
	 */
	public long getUseTime() {
		return useTime;
	}

	/**
	 * @param useTime
	 *          the useTime to set
	 */
	public void setUseTime(long useTime) {
		this.useTime = useTime;
	}
}
