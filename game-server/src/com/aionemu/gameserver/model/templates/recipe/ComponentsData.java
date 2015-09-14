package com.aionemu.gameserver.model.templates.recipe;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComponentsData")
public class ComponentsData {

	@XmlElement(name = "component")
	protected List<Component> component;

	public List<Component> getComponent() {
		return component;
	}

}
