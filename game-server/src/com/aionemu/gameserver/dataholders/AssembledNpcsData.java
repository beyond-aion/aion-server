package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.assemblednpc.AssembledNpcTemplate;

/**
 * @author xTz
 */
@XmlRootElement(name = "assembled_npcs")
@XmlAccessorType(XmlAccessType.FIELD)
public class AssembledNpcsData {

	@XmlElement(name = "assembled_npc", type = AssembledNpcTemplate.class)
	private List<AssembledNpcTemplate> templates;
	private final Map<Integer, AssembledNpcTemplate> assembledNpcsTemplates = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (AssembledNpcTemplate template : templates) {
			assembledNpcsTemplates.put(template.getNr(), template);
		}
		templates = null;
	}

	public int size() {
		return assembledNpcsTemplates.size();
	}

	public AssembledNpcTemplate getAssembledNpcTemplate(Integer i) {
		return assembledNpcsTemplates.get(i);
	}

}
