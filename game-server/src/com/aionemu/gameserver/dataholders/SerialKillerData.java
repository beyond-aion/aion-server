package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.serial_killer.RankRestriction;

/**
 * @author Dtem
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "rankRestriction" })
@XmlRootElement(name = "serial_killers")
public class SerialKillerData {

	@XmlElement(name = "rank_restriction")
	protected List<RankRestriction> rankRestriction;
	@XmlTransient
	private TIntObjectHashMap<RankRestriction> templates = new TIntObjectHashMap<RankRestriction>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (RankRestriction template : rankRestriction) {
			templates.put(template.getRankNum(), template);
		}
		rankRestriction.clear();
		rankRestriction = null;
	}

	public int size() {
		return templates.size();
	}

	public RankRestriction getRankRestriction(int rank) {
		return templates.get(rank);
	}

}
