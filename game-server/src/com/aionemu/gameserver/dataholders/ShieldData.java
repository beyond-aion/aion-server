package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.shield.ShieldTemplate;

/**
 * @author Wakizashi
 */
@XmlRootElement(name = "shields")
@XmlAccessorType(XmlAccessType.FIELD)
public class ShieldData {

	@XmlElement(name = "shield")
	private List<ShieldTemplate> shieldTemplates;

	public int size() {
		if (shieldTemplates == null) {
			shieldTemplates = new ArrayList<>();
			return 0;
		}
		return shieldTemplates.size();
	}

	public List<ShieldTemplate> getShieldTemplates() {
		if (shieldTemplates == null) {
			return new ArrayList<>();
		}
		return shieldTemplates;
	}

	public void addAll(Collection<ShieldTemplate> templates) {
		if (shieldTemplates == null) {
			shieldTemplates = new ArrayList<>();
		}
		shieldTemplates.addAll(templates);
	}
}
