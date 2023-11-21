package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.factions.NpcFactionTemplate;

/**
 * @author vlog
 */
@XmlRootElement(name = "npc_factions")
@XmlAccessorType(XmlAccessType.FIELD)
public class NpcFactionsData {

	@XmlElement(name = "npc_faction", required = true)
	protected List<NpcFactionTemplate> npcFactionsData;

	@XmlTransient
	private final Map<Integer, NpcFactionTemplate> factionsById = new HashMap<>();
	@XmlTransient
	private final Map<Integer, NpcFactionTemplate> factionsByNpcId = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		factionsById.clear();
		for (NpcFactionTemplate template : npcFactionsData) {
			factionsById.put(template.getId(), template);
			if (template.getNpcIds() != null) {
				for (Integer npcId : template.getNpcIds())
					factionsByNpcId.put(npcId, template);
			}
		}
		npcFactionsData = null;
	}

	public NpcFactionTemplate getNpcFactionById(int id) {
		return factionsById.get(id);
	}

	public NpcFactionTemplate getNpcFactionByNpcId(int id) {
		return factionsByNpcId.get(id);
	}

	public Collection<NpcFactionTemplate> getNpcFactionsData() {
		return factionsById.values();
	}

	public int size() {
		return factionsById.size();
	}
}
