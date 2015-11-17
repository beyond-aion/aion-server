package com.aionemu.gameserver.model.account;

import java.sql.Timestamp;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionMember;

/**
 * This class is holding information about player, that is displayed on char selection screen, such as: player commondata, player's appearance and
 * creation/deletion time.
 * 
 * @see PlayerCommonData
 * @see PlayerAppearance
 * @author Luno
 */
public class PlayerAccountData {

	private CharacterBanInfo cbi;
	private PlayerCommonData playerCommonData;
	private PlayerAppearance appereance;
	private List<Item> equipment;
	private Timestamp creationDate;
	private Timestamp deletionDate;
	private LegionMember legionMember;

	public PlayerAccountData(PlayerCommonData playerCommonData, CharacterBanInfo cbi, PlayerAppearance appereance, List<Item> equipment,
		LegionMember legionMember) {
		this.playerCommonData = playerCommonData;
		this.cbi = cbi;
		this.appereance = appereance;
		this.equipment = equipment;
		this.legionMember = legionMember;
	}

	public CharacterBanInfo getCharBanInfo() {
		return cbi;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	/**
	 * Sets deletion date.
	 * 
	 * @param deletionDate
	 */
	public void setDeletionDate(Timestamp deletionDate) {
		this.deletionDate = deletionDate;
	}

	/**
	 * Get deletion date.
	 * 
	 * @return Timestamp date when char should be deleted.
	 */
	public Timestamp getDeletionDate() {
		return deletionDate;
	}

	/**
	 * Get time in seconds when this player will be deleted ( 0 if player was not set to be deleted )
	 * 
	 * @return deletion time in seconds
	 */
	public int getDeletionTimeInSeconds() {
		return deletionDate == null ? 0 : (int) (deletionDate.getTime() / 1000);
	}

	/**
	 * @return the playerCommonData
	 */
	public PlayerCommonData getPlayerCommonData() {
		return playerCommonData;
	}

	/**
	 * @param playerCommonData
	 *          the playerCommonData to set
	 */
	public void setPlayerCommonData(PlayerCommonData playerCommonData) {
		this.playerCommonData = playerCommonData;
	}

	public PlayerAppearance getAppearance() {
		return appereance;
	}

	/**
	 * @param timestamp
	 */
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the legionMember
	 */
	public Legion getLegion() {
		return legionMember.getLegion();
	}

	/**
	 * Returns true if player is a legion member
	 * 
	 * @return true or false
	 */
	public boolean isLegionMember() {
		return legionMember != null;
	}

	/**
	 * @return the equipment
	 */
	public List<Item> getEquipment() {
		return equipment;
	}

	/**
	 * @param equipment
	 *          the equipment to set
	 */
	public void setEquipment(List<Item> equipment) {
		this.equipment = equipment;
	}
}
