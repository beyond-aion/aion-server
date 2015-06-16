package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.WarehouseExpandTemplate;

/**
 * This is for the Warehouse Expanders.
 * 
 * @author spufy
 */
@XmlRootElement(name = "warehouse_expander")
@XmlAccessorType(XmlAccessType.FIELD)
public class WarehouseExpandData {

	@XmlElement(name = "warehouse_npc")
	private List<WarehouseExpandTemplate> clist;
	private TIntObjectHashMap<WarehouseExpandTemplate> npctlistData = new TIntObjectHashMap<WarehouseExpandTemplate>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (WarehouseExpandTemplate npc : clist) {
			npctlistData.put(npc.getNpcId(), npc);
		}
	}

	public int size() {
		return npctlistData.size();
	}

	public WarehouseExpandTemplate getWarehouseExpandListTemplate(int id) {
		return npctlistData.get(id);
	}
}
