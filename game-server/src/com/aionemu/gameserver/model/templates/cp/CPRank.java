package com.aionemu.gameserver.model.templates.cp;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CPRank {

	@XmlAttribute(name = "type", required = true)
	private CPType type;
	@XmlAttribute(name = "rank_num", required = true)
	private int rankNum;
	@XmlAttribute(name = "visible_intruder_min_rank")
	private int visibleIntruderMinRank;
	@XmlElement(name = "modifiers")
	private ModifiersTemplate statModifiers;

	public CPType getType() {
		return type;
	}

	public int getRankNum() {
		return rankNum;
	}

	public List<StatFunction> getStatModifiers() {
		return statModifiers == null ? Collections.emptyList() : statModifiers.getModifiers();
	}

	public int getVisibleIntruderMinRank() {
		return visibleIntruderMinRank;
	}
}
