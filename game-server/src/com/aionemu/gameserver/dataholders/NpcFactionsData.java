package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.factions.NpcFactionTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author vlog
 */
@XmlRootElement(name = "npc_factions")
@XmlAccessorType(XmlAccessType.FIELD)
public class NpcFactionsData {

	@XmlElement(name = "npc_faction", required = true)
	protected List<NpcFactionTemplate> npcFactionsData;
	private TIntObjectHashMap<NpcFactionTemplate> factionsById = new TIntObjectHashMap<>();
	private TIntObjectHashMap<NpcFactionTemplate> factionsByNpcId = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		factionsById.clear();
		for (NpcFactionTemplate template : npcFactionsData) {
			factionsById.put(template.getId(), template);
			if (template.getNpcIds() != null) {
				for (Integer npcId : template.getNpcIds())
					factionsByNpcId.put(npcId, template);
			}
		}
	}

	public NpcFactionTemplate getNpcFactionById(int id) {
		return factionsById.get(id);
	}

	public NpcFactionTemplate getNpcFactionByNpcId(int id) {
		return factionsByNpcId.get(id);
	}

	public List<NpcFactionTemplate> getNpcFactionsData() {
		return npcFactionsData;
	}

	public int size() {
		return npcFactionsData.size();
	}
}
