package com.aionemu.gameserver.model.templates.gather;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

/**
 * @author KID
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Exmaterials", propOrder = { "material" })
public class ExMaterials {

	protected List<Material> material;

	public List<Material> getMaterial() {
		if (material == null) {
			material = new FastTable<>();
		}
		return this.material;
	}
}
