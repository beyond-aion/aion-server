package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.KillSpawned;

/**
 * @author vlog, Bobobear, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KillSpawnedData", propOrder = "monster")
public class KillSpawnedData extends MonsterHuntData {

	@XmlElement(name = "monster")
	protected List<Monster> monster;

	@Override
	public void register(QuestEngine questEngine) {
		questEngine.addQuestHandler(new KillSpawned(id, startNpcIds, endNpcIds, monster));
	}

	@Override
	public Set<Integer> getAlternativeNpcs(int npcId) {
		for (Monster m : monster) {
			List<Integer> npcIds = m.getNpcIds();
			if (npcIds != null && npcIds.size() > 1 && npcIds.contains(npcId))
				return npcIds.stream().filter(id -> id != npcId).collect(Collectors.toSet());
		}
		return super.getAlternativeNpcs(npcId);
	}
}
