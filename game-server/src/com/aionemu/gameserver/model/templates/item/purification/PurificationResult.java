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
@XmlRootElement(name = "PurificationResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class PurificationResult {

	@XmlAttribute(name = "result_item_id")
	private int resultItemId;
	@XmlAttribute(name = "min_enchant_count")
	private int minEnchantCount;
	@XmlAttribute(name = "necessary_abyss_points")
	private int necessaryAbyssPoints;
	@XmlAttribute(name = "necessary_kinah")
	private long necessaryKinah;
	@XmlElement(name = "req_material")
	private List<RequiredMaterial> requiredMaterials;

	public int getResultItemId() {
		return resultItemId;
	}

	public int getMinEnchantCount() {
		return minEnchantCount;
	}

	public int getNecessaryAbyssPoints() {
		return necessaryAbyssPoints;
	}

	public long getNecessaryKinah() {
		return necessaryKinah;
	}

	public List<RequiredMaterial> getRequiredMaterials() {
		return requiredMaterials;
	}
}
