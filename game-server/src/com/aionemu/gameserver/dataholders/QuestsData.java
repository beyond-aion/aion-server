package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "quests")
public class QuestsData {

	@XmlElement(name = "quest", required = true)
	private List<QuestTemplate> questsData;

	@XmlTransient
	private final Map<Integer, QuestTemplate> questTemplates = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<QuestTemplate>> sortedByFactionId = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		questTemplates.clear();
		sortedByFactionId.clear();
		for (QuestTemplate quest : questsData) {
			questTemplates.put(quest.getId(), quest);
			int npcFactionId = quest.getNpcFactionId();
			if (npcFactionId == 0 || quest.isTimeBased())
				continue;
			if (!sortedByFactionId.containsKey(npcFactionId)) {
				List<QuestTemplate> factionQuests = new ArrayList<>();
				factionQuests.add(quest);
				sortedByFactionId.put(npcFactionId, factionQuests);
			} else {
				sortedByFactionId.get(npcFactionId).add(quest);
			}
		}
		questsData = null;
	}

	public QuestTemplate getQuestById(int id) {
		return questTemplates.get(id);
	}

	public List<QuestTemplate> getQuestsByNpcFaction(int npcFactionId, Player player) {
		List<QuestTemplate> factionQuests = sortedByFactionId.get(npcFactionId);
		List<QuestTemplate> quests = new ArrayList<>();
		for (QuestTemplate questTemplate : factionQuests) {
			if (!QuestEngine.getInstance().isHaveHandler(questTemplate.getId()))
				continue;
			if (QuestService.checkStartConditions(player, questTemplate.getId(), false))
				quests.add(questTemplate);
		}
		return quests;
	}

	public int size() {
		return questTemplates.size();
	}

	public Collection<QuestTemplate> getQuestTemplates() {
		return questTemplates.values();
	}

}
