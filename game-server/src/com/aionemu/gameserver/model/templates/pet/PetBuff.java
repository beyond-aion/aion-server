package com.aionemu.gameserver.model.templates.pet;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetBuff", propOrder = { "modifiers" })
public class PetBuff {

	@XmlAttribute(required = true)
	protected int id;

	@XmlAttribute(name = "feed_count", required = true)
	protected int feedCount;

	@XmlElement(required = true)
	protected List<ModifiersTemplate> modifiers;

	/**
	 * Gets the value of the id property.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the value of the feedCount property.
	 */
	public int getFeedCount() {
		return feedCount;
	}

	/**
	 * Gets the value of the modifiers property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the modifiers property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getModifiers().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Modifiers }
	 */
	public List<ModifiersTemplate> getModifiers() {
		if (modifiers == null)
			modifiers = new ArrayList<>();
		return modifiers;
	}
}
