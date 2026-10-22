package com.aionemu.gameserver.model.templates.gather;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Materials", propOrder = { "material" })
public class Materials {

	protected List<Material> material;

	/**
	 * Gets the value of the material property.
	 */
	public List<Material> getMaterial() {
		if (material == null) {
			material = new ArrayList<>();
		}
		return this.material;
	}
}
