package com.aionemu.gameserver.model.templates.staticdoor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "World")
public class StaticDoorWorld {

	@XmlAttribute(name = "world")
	private int worldId;
	@XmlElement(name = "staticdoor")
	private List<StaticDoorTemplate> templates;
	@XmlTransient
	private Map<Integer, StaticDoorTemplate> templatesByStaticId;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		templatesByStaticId = new HashMap<>();
		for (StaticDoorTemplate template : templates) {
			if (templatesByStaticId.putIfAbsent(template.getId(), template) != null)
				throw new IllegalArgumentException("Duplicate door template for world " + worldId + ", id: " + template.getId());
		}
		templates = null;
	}

	public int getWorldId() {
		return worldId;
	}

	public Collection<StaticDoorTemplate> getStaticDoors() {
		return templatesByStaticId.values();
	}

	public StaticDoorTemplate getStaticDoor(int staticId) {
		return templatesByStaticId.get(staticId);
	}
}
