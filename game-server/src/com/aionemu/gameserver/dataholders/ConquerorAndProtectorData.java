package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.cp.CPRank;
import com.aionemu.gameserver.model.templates.cp.CPType;

/**
 * @author Dtem
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "conqueror_protector_ranks")
public class ConquerorAndProtectorData {

	@XmlElement(name = "rank")
	private List<CPRank> ranks;

	public CPRank getRank(CPType type, int rank) {
		for (CPRank template : ranks) {
			if (template.getType() == type && template.getRankNum() == rank)
				return template;
		}
		return null;
	}

	public int size() {
		return ranks.size();
	}
}
