package com.aionemu.gameserver.model.templates.pet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.change.Func;

/**
 * @author SVDNESS
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetPenaltyAttr")
public class PetPenaltyAttr {

	@XmlAttribute(required = true)
	protected StatEnum stat;
	@XmlAttribute(required = true)
	protected Func func;
	@XmlAttribute(required = true)
	protected int value;

	public StatEnum getStat() {
		return stat;
	}

	public void setStat(StatEnum value) {
		this.stat = value;
	}

	public Func getFunc() {
		return func;
	}

	public void setFunc(Func value) {
		this.func = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}