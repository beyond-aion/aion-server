package com.aionemu.gameserver.model.templates.arcadeupgrade;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ginho1
 */
@XmlType(name = "ArcadeTab")
public class ArcadeTab {

	@XmlAttribute(name = "id")
	private int id;
	@XmlElement(name = "item")
	private List<ArcadeTabItemList> arcadeTabItem;

	public int getId() {
		return id;
	}

	public List<ArcadeTabItemList> getArcadeTabItems() {
		return arcadeTabItem;
	}
}
