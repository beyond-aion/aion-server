package com.aionemu.gameserver.model.templates.spawns;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ViAl
 */
@XmlType(name = "town_spawn")
public class TownSpawn {

	@XmlAttribute(name = "level")
	public int level;
	@XmlElement(name = "spawn")
	public List<Spawn> spawns;
}
