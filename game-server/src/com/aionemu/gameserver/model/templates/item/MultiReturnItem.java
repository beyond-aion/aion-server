package com.aionemu.gameserver.model.templates.item;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ginho1
 */
@XmlType(name = "MultiReturnItem")
public class MultiReturnItem {

	@XmlAttribute(name = "id")
	private int id;
	@XmlElement(name = "return_loc")
	private List<ReturnLocList> returnLocList;

	public int getId() {
		return id;
	}

	public List<ReturnLocList> getReturnLocList() {
		return returnLocList;
	}
}
