package com.aionemu.gameserver.model.templates.item.purification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ranastic
 * @reworked Navyan
 */
@XmlRootElement(name = "PurificationResultItem")
@XmlAccessorType(XmlAccessType.FIELD)
public class PurificationResultItem {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute(name = "check_enchant_count")
	private int checkEnchantCount;
	@XmlElement(name = "required_materials")
	private RequiredMaterials requiredMaterials;
	@XmlElement(name = "kinah_needed")
	private NeedKinah requiredKinah;
	@XmlElement(name = "abyss_point_needed")
	private NeedAbyssPoint requiredAbyssPoints;

	/**
	 * @return the checkEnchantCount
	 */
	public int getCheckEnchantCount() {
		return checkEnchantCount;
	}

	/**
	 * @return the itemId
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * @return the requiredMaterials
	 */
	public RequiredMaterials getRequiredMaterials() {
		return requiredMaterials;
	}

	/**
	 * @return the requiredAbyssPoints
	 */
	public NeedAbyssPoint getRequiredAbyssPoints() {
		return requiredAbyssPoints;
	}

	/**
	 * @return the requiredKinah
	 */
	public NeedKinah getRequiredKinah() {
		return requiredKinah;
	}
}
