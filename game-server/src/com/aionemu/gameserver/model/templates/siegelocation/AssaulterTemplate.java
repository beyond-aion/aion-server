package com.aionemu.gameserver.model.templates.siegelocation;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.siege.AssaulterType;

/**
 * @author Estrayl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssaulterTemplate")
public class AssaulterTemplate {

	@XmlAttribute(name = "type")
	private AssaulterType assaulterType;
	@XmlAttribute(name = "heading_offset")
	private int headingOffset = 60;
	@XmlAttribute(name = "distance_offset")
	private int distanceOffset;

	@XmlList
	@XmlAttribute(name = "npc_ids")
	private List<Integer> npcIds;

	public AssaulterType getAssaulterType() {
		return assaulterType;
	}

	public List<Integer> getNpcIds() {
		return npcIds;
	}

	public int getHeadingOffset() {
		return headingOffset;
	}

	public int getDistanceOffset() {
		return distanceOffset;
	}
}
