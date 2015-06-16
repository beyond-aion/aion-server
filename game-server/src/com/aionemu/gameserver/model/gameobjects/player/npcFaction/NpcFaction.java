package com.aionemu.gameserver.model.gameobjects.player.npcFaction;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.PersistentState;

/**
 * @author MrPoke
 */
public class NpcFaction {

	private int id;
	private int time;
	private boolean active;
	private boolean mentor;
	private ENpcFactionQuestState state;
	private int questId;
	private PersistentState persistentState;

	/**
	 * @param id
	 * @param time
	 * @param active
	 * @param persistentState
	 * @param mentor
	 * @param state
	 */
	public NpcFaction(int id, int time, boolean active, ENpcFactionQuestState state, int questId) {
		this.id = id;
		this.time = time;
		this.active = active;
		this.state = state;
		this.mentor = DataManager.NPC_FACTIONS_DATA.getNpcFactionById(id).isMentor();
		this.questId = questId;
		this.persistentState = PersistentState.NEW;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return the mentor
	 */
	public boolean isMentor() {
		return mentor;
	}

	/**
	 * @return the state
	 */
	public ENpcFactionQuestState getState() {
		return state;
	}

	/**
	 * @param time
	 *          the time to set
	 */
	public void setTime(int time) {
		this.time = time;
		this.setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @param active
	 *          the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
		this.setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @param state
	 *          the state to set
	 */
	public void setState(ENpcFactionQuestState state) {
		this.setPersistentState(PersistentState.UPDATE_REQUIRED);
		this.state = state;
	}

	/**
	 * @return the questId
	 */
	public int getQuestId() {
		return questId;
	}

	/**
	 * @param questId
	 *          the questId to set
	 */
	public void setQuestId(int questId) {
		this.questId = questId;
		this.setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return the persistentState
	 */
	public PersistentState getPersistentState() {
		return persistentState;
	}

	/**
	 * @param persistentState
	 *          the persistentState to set
	 */
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case DELETED:
				if (this.persistentState == PersistentState.NEW)
					this.persistentState = PersistentState.NOACTION;
				else
					this.persistentState = PersistentState.DELETED;
				break;
			case UPDATE_REQUIRED:
				if (this.persistentState != PersistentState.NEW)
					this.persistentState = PersistentState.UPDATE_REQUIRED;
				break;
			case NOACTION:
				break;
			default:
				this.persistentState = persistentState;
		}
	}

}
