package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.teleport.TeleporterTemplate;

/**
 * This is a container holding and serving all {@link NpcTemplate} instances.<br>
 * Briefly: Every {@link Npc} instance represents some class of NPCs among which each have the same id, name, items, statistics. Data for such NPC
 * class is defined in {@link NpcTemplate} and is uniquely identified by npc id.
 * 
 * @author orz
 */
@XmlRootElement(name = "npc_teleporter")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeleporterData {

	@XmlElement(name = "teleporter_template")
	private List<TeleporterTemplate> templates;

	@XmlTransient
	private final Map<Integer, TeleporterTemplate> teleporterTemplates = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (TeleporterTemplate template : templates) {
			teleporterTemplates.put(template.getTeleportId(), template);
		}
		templates = null;
	}

	public int size() {
		return teleporterTemplates.size();
	}

	public TeleporterTemplate getTeleporterTemplateByNpcId(int npcId) {
		for (TeleporterTemplate template : teleporterTemplates.values()) {
			if (template.containNpc(npcId)) {
				return template;
			}
		}
		return null;
	}

	public TeleporterTemplate getTeleporterTemplateByTeleportId(int teleportId) {
		return teleporterTemplates.get(teleportId);
	}
}
