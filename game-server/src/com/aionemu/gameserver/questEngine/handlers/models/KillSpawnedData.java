package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastMap;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.KillSpawned;

/**
 * @author vlog
 * @modified Bobobear, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KillSpawnedData", propOrder = "monster")
public class KillSpawnedData extends MonsterHuntData {

	@XmlElement(name = "monster")
	protected List<Monster> monster;
	
	@Override
	public void register(QuestEngine questEngine) {
		FastMap<List<Integer>, Monster> spawnedMonsters = new FastMap<>();
		for (Monster m : monster)
			spawnedMonsters.put(m.getNpcIds(), m);
		questEngine.addQuestHandler(new KillSpawned(id, startNpcIds, endNpcIds, spawnedMonsters));
	}
}
