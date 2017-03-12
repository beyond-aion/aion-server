package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;

/**
 * @author M@xx
 */
@XmlRootElement(name = "fly_rings")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlyRingData {

	@XmlElement(name = "fly_ring")
	private List<FlyRingTemplate> flyRingTemplates;

	public int size() {
		if (flyRingTemplates == null) {
			flyRingTemplates = new ArrayList<>();
			return 0;
		}
		return flyRingTemplates.size();
	}

	public List<FlyRingTemplate> getFlyRingTemplates() {
		if (flyRingTemplates == null) {
			return new ArrayList<>();
		}
		return flyRingTemplates;
	}

	public void addAll(Collection<FlyRingTemplate> templates) {
		if (flyRingTemplates == null) {
			flyRingTemplates = new ArrayList<>();
		}
		flyRingTemplates.addAll(templates);
	}
}
