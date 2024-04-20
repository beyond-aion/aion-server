package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.base.BaseTemplate;

/**
 * @author Source, Estrayl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "base_locations")
public class BaseData {

	@XmlElement(name = "base_location")
	private List<BaseTemplate> baseTemplates;

	public int size() {
		return baseTemplates.size();
	}

	public List<BaseTemplate> getAllBaseTemplates() {
		return baseTemplates;
	}
}