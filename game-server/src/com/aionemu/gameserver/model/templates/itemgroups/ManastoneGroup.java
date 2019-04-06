package com.aionemu.gameserver.model.templates.itemgroups;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ManastoneGroup complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ManastoneGroup">
 *   &lt;complexContent>
 *     &lt;extension base="{}BonusItemGroup">
 *       &lt;sequence>
 *         &lt;element name="item" type="{}ItemRaceEntry" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ManastoneGroup")
public class ManastoneGroup extends BonusItemGroup {

	@XmlElement(name = "item")
	private List<ItemRaceEntry> items;

	@Override
	public List<ItemRaceEntry> getItems() {
		return items == null ? Collections.emptyList() : items;
	}

}
