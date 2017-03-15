package com.aionemu.gameserver.dataholders;

import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.monsterraid.MonsterRaidLocation;
import com.aionemu.gameserver.model.templates.monsterraid.MonsterRaidTemplate;

/**
 * @author Alcapwnd
 * @modified Whoop
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "raid_locations")
public class MonsterRaidData {

	@XmlElement(name = "raid_location")
	private List<MonsterRaidTemplate> monsterRaidTemplates;
	@XmlTransient
	private LinkedHashMap<Integer, MonsterRaidLocation> monsterRaid = new LinkedHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (MonsterRaidTemplate template : monsterRaidTemplates)
			monsterRaid.put(template.getLocationId(), new MonsterRaidLocation(template));
	}

	public int size() {
		return monsterRaid.size();
	}

	public LinkedHashMap<Integer, MonsterRaidLocation> getRaidLocations() {
		return monsterRaid;
	}
}
