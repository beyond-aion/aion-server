package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.StorageExpansionTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

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
	private TIntObjectHashMap<StorageExpansionTemplate> expansionTemplatesByNpcId = new TIntObjectHashMap<>();

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
