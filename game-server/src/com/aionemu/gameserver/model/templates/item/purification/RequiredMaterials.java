package com.aionemu.gameserver.model.templates.item.purification;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ranastic
 * @reworked Navyan
 */
@XmlRootElement(name = "RequiredMaterials")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequiredMaterials {

	@XmlElement(name = "sub_material_item", required = true)
	protected List<SubMaterialItem> subMaterialItems;

	/**
	 * @return the subMaterialItem
	 */
	public List<SubMaterialItem> getSubMaterialItems() {
		return subMaterialItems;
	}
}
