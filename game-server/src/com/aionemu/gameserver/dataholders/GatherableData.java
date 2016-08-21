package com.aionemu.gameserver.dataholders;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ATracer
 */
@XmlRootElement(name = "gatherable_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class GatherableData {

	@XmlElement(name = "gatherable_template")
	private List<GatherableTemplate> gatherables;

	/** A map containing all npc templates */
	private TIntObjectHashMap<GatherableTemplate> gatherableData = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (GatherableTemplate gatherable : gatherables) {
			if (gatherable.getMaterials() != null)
				Collections.sort(gatherable.getMaterials().getMaterial());
			if (gatherable.getExtraMaterials() != null)
				Collections.sort(gatherable.getExtraMaterials().getMaterial());
			gatherableData.put(gatherable.getTemplateId(), gatherable);
		}
		gatherables = null;
	}

	public int size() {
		return gatherableData.size();
	}

	/**
	 * /** Returns an {@link GatherableTemplate} object with given id.
	 * 
	 * @param id
	 *          id of GatherableTemplate
	 * @return GatherableTemplate object containing data about Gatherable with that id.
	 */
	public GatherableTemplate getGatherableTemplate(int id) {
		return gatherableData.get(id);
	}
}
