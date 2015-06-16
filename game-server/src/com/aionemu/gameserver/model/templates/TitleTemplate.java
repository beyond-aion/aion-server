package com.aionemu.gameserver.model.templates;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author xavier
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "title_templates")
public class TitleTemplate implements StatOwner {

	@XmlAttribute(name = "id", required = true)
	@XmlID
	private String id;

	@XmlElement(name = "modifiers", required = false)
	protected ModifiersTemplate modifiers;

	@XmlAttribute(name = "race", required = true)
	private Race race;

	private int titleId;
	
	@XmlAttribute(name = "nameId")
	private int nameId;
	
	@XmlAttribute(name = "desc")
	private String description;

	public int getTitleId() {
		return titleId;
	}

	public Race getRace() {
		return race;
	}
	
	public int getNameId() {
		return nameId;
	}
	
	public String getDesc() {
		return description;
	}

	public List<StatFunction> getModifiers() {
		if (modifiers != null) {
			return modifiers.getModifiers();
		}
		return null;
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		this.titleId = Integer.parseInt(id);
	}

}
