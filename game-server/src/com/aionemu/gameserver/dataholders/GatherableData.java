package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "gatherable_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class GatherableData {

	@XmlElement(name = "gatherable_template")
	private List<GatherableTemplate> gatherables;

	@XmlTransient
	private final Map<Integer, GatherableTemplate> gatherableData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (GatherableTemplate gatherable : gatherables) {
			if (gatherable.getMaterials() != null)
				gatherable.getMaterials().getMaterial().sort(null);
			if (gatherable.getExtraMaterials() != null)
				gatherable.getExtraMaterials().getMaterial().sort(null);
			gatherableData.put(gatherable.getTemplateId(), gatherable);
		}
		gatherables = null;
	}

	public int size() {
		return gatherableData.size();
	}

	public GatherableTemplate getGatherableTemplate(int id) {
		return gatherableData.get(id);
	}
}
