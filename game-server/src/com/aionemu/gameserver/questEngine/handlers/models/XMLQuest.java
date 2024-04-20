package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;

/**
 * @author MrPoke, Hilgert, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestScriptData")
@XmlSeeAlso({ CraftingRewardsData.class, FountainRewardsData.class, ItemCollectingData.class, ItemOrdersData.class, KillInWorldData.class,
	KillInZoneData.class, KillSpawnedData.class, MentorMonsterHuntData.class, MonsterHuntData.class, RelicRewardsData.class, ReportOnLevelUpData.class,
	ReportToData.class, ReportToManyData.class, SkillUseData.class, WorkOrdersData.class, XmlQuestData.class })
public abstract class XMLQuest {

	@XmlAttribute(name = "id", required = true)
	protected int id;

	@XmlAttribute(name = "movie")
	protected int questMovie;

	@XmlAttribute(name = "mission")
	protected boolean mission;
	
	public int getId() {
		return id;
	}

	public abstract void register(QuestEngine questEngine);

	public abstract Set<Integer> getAlternativeNpcs(int npcId);
}
