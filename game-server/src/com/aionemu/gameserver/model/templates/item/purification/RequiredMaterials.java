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

	@XmlElement(required = true)
	protected List<SubMaterialItem> sub_material_item;

	/**
	 * @return the subMaterialItem
	 */
	public List<SubMaterialItem> getSubMaterialItem() {
		return sub_material_item;
	}
}
