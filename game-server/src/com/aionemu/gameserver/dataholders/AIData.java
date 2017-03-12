package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.ai.AITemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author xTz
 */
@XmlRootElement(name = "ai_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class AIData {

	@XmlElement(name = "ai", type = AITemplate.class)
	private List<AITemplate> templates;
	private TIntObjectHashMap<AITemplate> aiTemplate = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		aiTemplate.clear();
		for (AITemplate template : templates)
			aiTemplate.put(template.getNpcId(), template);
		templates = null;
	}

	public int size() {
		return aiTemplate.size();
	}

	public AITemplate getAiTemplate(int npcId) {
		return aiTemplate.get(npcId);
	}
}
