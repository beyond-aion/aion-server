package com.aionemu.gameserver.skillengine.model;

/**
 * @author kecimis
 */
public class EffectReserved implements Comparable<EffectReserved> {

	private int position;
	private int value;
	private ResourceType type;
	private boolean isDamage = true;
	private boolean send = true;

	public static enum ResourceType {

		HP(0),
		MP(1),
		FP(2),
		DP(3);// TODO recheck

		private int value;

		private ResourceType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}

		public static ResourceType of(HealType healType) {
			return valueOf(healType.name());
		}
	}

	public EffectReserved(int position, int value, ResourceType type, boolean isDamage) {
		this(position, value, type, isDamage, true);
	}

	public EffectReserved(int position, int value, ResourceType type, boolean isDamage, boolean send) {
		this.position = position;
		this.value = value;
		this.type = type;
		this.isDamage = isDamage;
		this.send = send;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	public int getValueToSend() {
		if (isDamage)
			return this.value;
		else
			return -this.value;
	}

	/**
	 * @return the type
	 */
	public ResourceType getType() {
		return type;
	}

	/**
	 * @return the isDamage
	 */
	public boolean isDamage() {
		return isDamage;
	}

	/**
	 * @return the send
	 */
	public boolean isSend() {
		return send;
	}

	@Override
	public int compareTo(EffectReserved o) {
		int result = 0;
		if (this.position < o.getPosition())
			result = -1;
		else if (this.position > o.getPosition())
			result = 1;

		if (result == 0)
			result = this.hashCode() - o.hashCode();

		return result;
	}
}
