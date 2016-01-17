package com.aionemu.gameserver.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yeats
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LegionDominionReward")
public class LegionDominionReward {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "count")
	protected int count;
	@XmlAttribute(name = "time")
	protected int time;
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}
	
	
}
