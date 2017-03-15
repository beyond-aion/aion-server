package com.aionemu.gameserver.model.templates.itemgroups;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.rewards.IdLevelReward;

/**
 * @author Rolandas
 *
 */

/**
 * <p>
 * Java class for MedicineGroup complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MedicineGroup">
 *   &lt;complexContent>
 *     &lt;extension base="{}BonusItemGroup">
 *       &lt;sequence>
 *         &lt;element name="item" type="{}IdLevelReward" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedicineGroup")
public class MedicineGroup extends BonusItemGroup {

	@XmlElement(name = "item")
	protected List<IdLevelReward> items;

	/**
	 * Gets the value of the item property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the item property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getItems().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link IdLevelReward }
	 */
	public List<IdLevelReward> getItems() {
		if (items == null) {
			items = new ArrayList<>();
		}
		return this.items;
	}

	@Override
	public ItemRaceEntry[] getRewards() {
		return getItems().toArray(new ItemRaceEntry[0]);
	}

}
