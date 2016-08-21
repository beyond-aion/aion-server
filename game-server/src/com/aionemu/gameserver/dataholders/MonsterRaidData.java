package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.monsterraid.MonsterRaidLocation;
import com.aionemu.gameserver.model.templates.monsterraid.MonsterRaidTemplate;

import javolution.util.FastMap;

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
	private FastMap<Integer, MonsterRaidLocation> monsterRaid = new FastMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (MonsterRaidTemplate template : monsterRaidTemplates)
			monsterRaid.put(template.getLocationId(), new MonsterRaidLocation(template));
	}

	public int size() {
		return monsterRaid.size();
	}

	public FastMap<Integer, MonsterRaidLocation> getRaidLocations() {
		return monsterRaid;
	}
}
