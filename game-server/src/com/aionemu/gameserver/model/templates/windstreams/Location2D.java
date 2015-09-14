package com.aionemu.gameserver.model.templates.windstreams;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.flypath.FlyPathType;

/**
 * @author LokiReborn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Location2D")
public class Location2D {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "state")
	protected int state;
	@XmlAttribute(name = "fly_path")
	protected FlyPathType flyPath;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	/**
	 * @return the bidirectional
	 */
	public FlyPathType getFlyPathType() {
		return flyPath;
	}
}
