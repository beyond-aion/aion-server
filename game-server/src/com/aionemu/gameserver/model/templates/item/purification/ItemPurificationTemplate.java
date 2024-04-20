package com.aionemu.gameserver.model.templates.item.purification;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ranastic, Navyan, Estrayl
 */
@XmlRootElement(name = "ItemPurification")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemPurificationTemplate {

	@XmlElement(name = "purification_result", required = true)
	private List<PurificationResult> purificationResults;
	@XmlAttribute(name = "base_item_id")
	private int baseItemId;

	public List<PurificationResult> getPurificationResults() {
		return purificationResults;
	}

	public int getBaseItemId() {
		return baseItemId;
	}
}
