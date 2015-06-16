package com.aionemu.gameserver.model.account;

/**
 * @author cura
 */
public class CharacterPasskey {

	private int objectId;
	private int wrongCount = 0;
	private boolean isPass = false;
	private ConnectType connectType;

	/**
	 * @return the objectId
	 */
	public int getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId
	 *          the objectId to set
	 */
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return the wrongCount
	 */
	public int getWrongCount() {
		return wrongCount;
	}

	/**
	 * @param count
	 *          the wrongCount to set
	 */
	public void setWrongCount(int count) {
		this.wrongCount = count;
	}

	/**
	 * @return the isPass
	 */
	public boolean isPass() {
		return isPass;
	}

	/**
	 * @param isPass
	 *          the isPass to set
	 */
	public void setIsPass(boolean isPass) {
		this.isPass = isPass;
	}

	/**
	 * @return the connectType
	 */
	public ConnectType getConnectType() {
		return connectType;
	}

	/**
	 * @param connectType
	 *          the connectType to set
	 */
	public void setConnectType(ConnectType connectType) {
		this.connectType = connectType;
	}

	public enum ConnectType {
		ENTER,
		DELETE
	}
}
