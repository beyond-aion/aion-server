package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.LegionDominionLocationTemplate;

/**
 * @author Yeats
 *
 */
@XmlRootElement(name="legion_dominion_template")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegionDominionData {
	
	@XmlElement(name = "legion_dominion_location")
	private List<LegionDominionLocationTemplate> ldl;
	
	public int size() {
		return ldl.size();
	}

	public List<LegionDominionLocationTemplate> getLocationTemplates() {
		return ldl;
	}
}
