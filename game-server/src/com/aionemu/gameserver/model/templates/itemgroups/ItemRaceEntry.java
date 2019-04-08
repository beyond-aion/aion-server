package com.aionemu.gameserver.model.templates.itemgroups;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.ItemData;
import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.dataholders.loadingutils.StaticDataListener;
import com.aionemu.gameserver.model.Chance;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.rewards.IdLevelReward;

/**
 * <p>
 * Java class for ItemRaceEntry complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ItemRaceEntry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="race" type="{}Race" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemRaceEntry")
@XmlSeeAlso({ IdLevelReward.class })
public class ItemRaceEntry implements Chance {

	@XmlAttribute(name = "id", required = true)
	private int id;

	@XmlAttribute(name = "race")
	private Race race = Race.PC_ALL;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		StaticData staticData = StaticDataListener.get(u);
		ItemData itemData = staticData != null ? staticData.itemData : DataManager.ITEM_DATA;
		ItemTemplate itemTemplate = itemData.getItemTemplate(id);
		if (itemTemplate == null)
			throw new IllegalArgumentException("BonusItemGroup item ID " + id + " is invalid");
		if (itemTemplate.getRace() != Race.PC_ALL && race != Race.PC_ALL && itemTemplate.getRace() != race)
			throw new IllegalArgumentException("BonusItemGroup item " + id + " has invalid race " + race + ". Item is only for " + itemTemplate.getRace());
	}

	public int getId() {
		return id;
	}

	public Race getRace() {
		return race;
	}

	public long getCount() {
		return 1L;
	}

	@Override
	public float getChance() {
		return 100f;
	}

	public final boolean matches(Race playerRace, QuestTemplate questTemplate) {
		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(id);
		if (!matchesRace(itemTemplate, playerRace))
			return false;
		if (!matchesLevel(itemTemplate, questTemplate.getBonus().getLevel()))
			return false;
		if (!matchesQuest(questTemplate))
			return false;
		return true;
	}

	protected boolean matchesQuest(QuestTemplate questTemplate) {
		return true;
	}

	protected boolean matchesLevel(ItemTemplate itemTemplate, int bonusItemLevel) {
		return bonusItemLevel == 0 || bonusItemLevel == itemTemplate.getLevel();
	}

	/**
	 * Method is used to check item race; Some items having PC_ALL really are not for both races, like some foods and weapons
	 * 
	 * @param playerRace
	 *          player's race
	 * @return true if race is correct for player when overridden or not from templates
	 */
	private boolean matchesRace(ItemTemplate itemTemplate, Race playerRace) {
		if (itemTemplate.getRace() != Race.PC_ALL && itemTemplate.getRace() != playerRace)
			return false;
		if (race != Race.PC_ALL && race != playerRace)
			return false;
		return true;
	}

}
