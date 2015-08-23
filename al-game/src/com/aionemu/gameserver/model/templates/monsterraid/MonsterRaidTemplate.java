package com.aionemu.gameserver.model.templates.monsterraid;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;


/**
 * @author Alcapwnd
 * @modified Whoop
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Raid")
public class MonsterRaidTemplate {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "world")
	protected int world;
	@XmlAttribute(name = "x")
	protected float x;
	@XmlAttribute(name = "y")
	protected float y;
	@XmlAttribute(name = "z")
	protected float z;
	@XmlAttribute(name = "h")
	protected byte h;
	@XmlList
	@XmlAttribute(name = "npc_id")
	protected List<Integer> npc_ids;

	public int getLocationId() {
		return this.id;
	}

	public int getWorldId() {
		return this.world;
	}
	
	public List<Integer> getNpcIds() {
		if (npc_ids == null)
			return Collections.emptyList();
		return npc_ids;
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public float getZ() {
		return this.z;
	}
    
	public byte getH() {
		return this.h;
	}
}
