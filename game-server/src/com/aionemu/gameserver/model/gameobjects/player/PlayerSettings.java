package com.aionemu.gameserver.model.gameobjects.player;

import com.aionemu.gameserver.model.gameobjects.Persistable;

/**
 * @author ATracer
 */
public class PlayerSettings implements Persistable {

	private PersistentState persistentState;

	private byte[] uiSettings;
	private byte[] shortcuts;
	private byte[] houseBuddies;
	private int deny = 0;
	private int display = 0;

	public PlayerSettings() {
	}

	public PlayerSettings(byte[] uiSettings, byte[] shortcuts, byte[] houseBuddies, int deny, int display) {
		this.uiSettings = uiSettings;
		this.shortcuts = shortcuts;
		this.houseBuddies = houseBuddies;
		this.deny = deny;
		this.display = display;
	}

	/**
	 * @return the persistentState
	 */
	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	/**
	 * @param persistentState
	 *          the persistentState to set
	 */
	@Override
	public void setPersistentState(PersistentState persistentState) {
		this.persistentState = persistentState;
	}

	/**
	 * @return the uiSettings
	 */
	public byte[] getUiSettings() {
		return uiSettings;
	}

	/**
	 * @param uiSettings
	 *          the uiSettings to set
	 */
	public void setUiSettings(byte[] uiSettings) {
		this.uiSettings = uiSettings;
		persistentState = PersistentState.UPDATE_REQUIRED;
	}

	/**
	 * @return the shortcuts
	 */
	public byte[] getShortcuts() {
		return shortcuts;
	}

	/**
	 * @param shortcuts
	 *          the shortcuts to set
	 */
	public void setShortcuts(byte[] shortcuts) {
		this.shortcuts = shortcuts;
		persistentState = PersistentState.UPDATE_REQUIRED;
	}

	/**
	 * @return the houseBuddies
	 */
	public byte[] getHouseBuddies() {
		return houseBuddies;
	}

	/**
	 * @param houseBuddies
	 *          the houseBuddies to set
	 */
	public void setHouseBuddies(byte[] houseBuddies) {
		this.houseBuddies = houseBuddies;
		persistentState = PersistentState.UPDATE_REQUIRED;
	}

	/**
	 * @return the display
	 */
	public int getDisplay() {
		return display;
	}

	/**
	 * @param display
	 *          the display to set
	 */
	public void setDisplay(int display) {
		this.display = display;
		persistentState = PersistentState.UPDATE_REQUIRED;
	}

	/**
	 * @return the deny
	 */
	public int getDeny() {
		return deny;
	}

	/**
	 * @param deny
	 *          the deny to set
	 */
	public void setDeny(int deny) {
		this.deny = deny;
		persistentState = PersistentState.UPDATE_REQUIRED;
	}

	public boolean isInDeniedStatus(DeniedStatus deny) {
		int isDeniedStatus = this.deny & deny.getId();

		if (isDeniedStatus == deny.getId())
			return true;

		return false;
	}
}
