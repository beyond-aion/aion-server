package com.aionemu.gameserver.model.templates;

import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.expand.Expand;

/**
 * @author Simple
 */
@XmlRootElement(name = "expansion_npc")
@XmlAccessorType(XmlAccessType.FIELD)
public class StorageExpansionTemplate {

	@XmlElement(name = "expand", required = true)
	private List<Expand> expansions;

	@XmlAttribute(name = "ids", required = true)
	private int[] ids;

	public int[] getNpcIds() {
		return ids;
	}

	public int getMinExpansionLevel() {
		return expansions.stream().mapToInt(Expand::getLevel).min().orElse(0);
	}

	public int getMaxExpansionLevel() {
		return expansions.stream().mapToInt(Expand::getLevel).max().orElse(0);
	}

	public Integer getPrice(int level) {
		for (Expand expand : expansions) {
			if (expand.getLevel() == level)
				return expand.getPrice();
		}
		return null;
	}
}
