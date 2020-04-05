package com.aionemu.gameserver.model.templates;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author xavier
 */
@XmlAccessorType(XmlAccessType.NONE)
public class TitleTemplate implements StatOwner, L10n {

	@XmlAttribute(name = "id", required = true)
	private int titleId;
	@XmlElement(name = "modifiers")
	protected ModifiersTemplate modifiers;
	@XmlAttribute(name = "race", required = true)
	private Race race;
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

	@Override
	public int getL10nId() {
		return nameId;
	}

	public String getDesc() {
		return description;
	}

	public List<StatFunction> getModifiers() {
		return modifiers == null ? null : modifiers.getModifiers();
	}
}
