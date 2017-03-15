package com.aionemu.gameserver.model.templates.quest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CollectItems", propOrder = { "collectItem" })
public class CollectItems {

	@XmlElement(name = "collect_item")
	protected List<CollectItem> collectItem;

	@XmlAttribute(name = "start_check")
	protected Boolean startCheck;

	/**
	 * Gets the value of the collectItem property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the collectItem property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCollectItem().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link CollectItem }
	 */
	public List<CollectItem> getCollectItem() {
		if (collectItem == null) {
			collectItem = new ArrayList<>();
		}
		return this.collectItem;
	}

	public boolean getStartCheck() {
		if (startCheck == null)
			return false;
		return startCheck;
	}

}
