package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LBox implements Cloneable {

	@XmlElement(required = true)
	protected int id;
	
	@XmlElement(required = true)
	protected String name;

	@XmlElement(required = true)
	protected String desc;

	@XmlElement(required = true)
	protected String script;

	@XmlElement(required = true)
	protected int icon;

	public int getId() {
		return id;
	}
	
	public void setId(int position) {
		id = 100 + position;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public String getScript() {
		return script;
	}

	public int getIcon() {
		return icon;
	}
	
	public void setIcon(int id) {
		icon = id;
	}
	
	@Override
	public Object clone() {
		LBox result = new LBox();
		result.id = this.id;
		result.name = this.name;
		result.desc = this.desc;
		result.script = this.script;
		result.icon = this.icon;
		
		return result;
	}

}
