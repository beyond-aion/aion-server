package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.teleport.TeleporterTemplate;

/**
 * This is a container holding and serving all {@link NpcTemplate} instances.<br>
 * Briefly: Every {@link Npc} instance represents some class of NPCs among which each have the same id, name, items,
 * statistics. Data for such NPC class is defined in {@link NpcTemplate} and is uniquely identified by npc id.
 * 
 * @author orz
 */
@XmlRootElement(name = "npc_teleporter")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeleporterData {

	@XmlElement(name = "teleporter_template")
	private List<TeleporterTemplate> tlist;

	/** A map containing all trade list templates */
	private TIntObjectHashMap<TeleporterTemplate> npctlistData = new TIntObjectHashMap<TeleporterTemplate>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (TeleporterTemplate template : tlist) {
			npctlistData.put(template.getTeleportId(), template);
		}
	}

	public int size() {
		return npctlistData.size();
	}

	public TeleporterTemplate getTeleporterTemplateByNpcId(int npcId) {
		for (TeleporterTemplate template : npctlistData.valueCollection()) {
			if (template.containNpc(npcId)) {
				return template;
			}
		}
		return null;
	}

	public TeleporterTemplate getTeleporterTemplateByTeleportId(int teleportId) {
		return npctlistData.get(teleportId);
	}
}
