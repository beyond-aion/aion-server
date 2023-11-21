package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.ai.AITemplate;

/**
 * @author xTz
 */
@XmlRootElement(name = "ai_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class AIData {

	@XmlElement(name = "ai", type = AITemplate.class)
	private List<AITemplate> templates;

	@XmlTransient
	private final Map<Integer, AITemplate> aiTemplate = new HashMap<>();

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
