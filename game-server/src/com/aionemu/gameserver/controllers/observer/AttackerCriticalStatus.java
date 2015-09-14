package com.aionemu.gameserver.controllers.observer;

/**
 * @author kecimis
 */
public class AttackerCriticalStatus {

	private boolean result = false;
	private int count;
	private int value;
	private boolean isPercent;

	public AttackerCriticalStatus(boolean result) {
		this.result = result;
	}

	public AttackerCriticalStatus(int count, int value, boolean isPercent) {
		this.count = count;
		this.value = value;
		this.isPercent = isPercent;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count
	 *          the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return the isPercent
	 */
	public boolean isPercent() {
		return isPercent;
	}

	/**
	 * @return the result
	 */
	public boolean isResult() {
		return result;
	}

	/**
	 * @param result
	 *          the result to set
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

}
