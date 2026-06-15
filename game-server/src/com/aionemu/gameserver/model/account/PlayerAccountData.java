package com.aionemu.gameserver.model.account;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.templates.BoundRadius;

/**
 * This class is holding information about player, that is displayed on char selection screen, such as: player commondata, player's appearance and
 * creation/deletion time.
 * 
 * @see PlayerCommonData
 * @see PlayerAppearance
 * @author Luno
 */
public class PlayerAccountData {

	private final PlayerCommonData playerCommonData;
	private PlayerAppearance appearance;
	private CharacterBanInfo cbi;
	private List<VisibleItem> visibleItems;
	private Timestamp creationDate;
	private Timestamp deletionDate;

	public PlayerAccountData(PlayerCommonData playerCommonData, PlayerAppearance appearance) {
		this(playerCommonData, appearance, null, Collections.emptyList());
	}

	public PlayerAccountData(PlayerCommonData playerCommonData, PlayerAppearance appearance, CharacterBanInfo cbi, List<VisibleItem> visibleItems) {
		this.playerCommonData = playerCommonData;
		this.appearance = appearance;
		this.cbi = cbi;
		this.visibleItems = visibleItems;
		updateBoundingRadius();
	}

	public CharacterBanInfo getCharBanInfo() {
		return cbi;
	}

	public void setCharBanInfo(CharacterBanInfo cbi) {
		this.cbi = cbi;
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

	public PlayerAppearance getAppearance() {
		return appearance;
	}

	public void setAppearance(PlayerAppearance appearance) {
		this.appearance = appearance;
		updateBoundingRadius();
	}

	public void updateBoundingRadius() {
		playerCommonData.setBoundingRadius(new BoundRadius(0.25f, 0.25f, appearance.getBoundHeight()));
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public List<VisibleItem> getVisibleItems() {
		return visibleItems;
	}

	public void setVisibleItems(List<Item> equipment) {
		List<VisibleItem> items = new ArrayList<>();
		for (Item item : equipment) {
			byte slotType = ItemSlot.getEquipmentSlotType(item.getEquipmentSlot());
			if (slotType != 0)
				items.add(new VisibleItem(slotType, item.getItemSkinTemplate().getTemplateId(), item.getGodStoneId(), item.getItemColor()));
		}
		this.visibleItems = items;
	}

	public record VisibleItem(byte slotType, int itemId, int godStoneId, Integer color) {}
}
