package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.StorageExpansionTemplate;

/**
 * This is for the Warehouse Expanders.
 * 
 * @author spufy
 */
@XmlRootElement(name = "warehouse_expander")
@XmlAccessorType(XmlAccessType.FIELD)
public class WarehouseExpandData {

	@XmlElement(name = "expansion_npc")
	private List<StorageExpansionTemplate> expansionTemplates;

	@XmlTransient
	private final Map<Integer, StorageExpansionTemplate> expansionTemplatesByNpcId = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (StorageExpansionTemplate expansionTemplate : expansionTemplates) {
			for (int npcId : expansionTemplate.getNpcIds())
				expansionTemplatesByNpcId.put(npcId, expansionTemplate);
		}
		expansionTemplates = null;
	}

	public int size() {
		return expansionTemplatesByNpcId.size();
	}

	public StorageExpansionTemplate getWarehouseExpansionTemplate(int id) {
		return expansionTemplatesByNpcId.get(id);
	}
}
