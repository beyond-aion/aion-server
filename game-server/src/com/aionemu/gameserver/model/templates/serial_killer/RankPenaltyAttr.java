package com.aionemu.gameserver.model.templates.serial_killer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.change.Func;

/**
 * @author Dtem
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RankPenaltyAttr")
public class RankPenaltyAttr {

	@XmlAttribute(required = true)
	protected StatEnum stat;
	@XmlAttribute(required = true)
	protected Func func;
	@XmlAttribute(required = true)
	protected int value;

	/**
	 * Gets the value of the stat property.
	 * 
	 * @return possible object is {@link StatEnum }
	 */
	public StatEnum getStat() {
		return stat;
	}

	/**
	 * Sets the value of the stat property.
	 * 
	 * @param value
	 *          allowed object is {@link StatEnum }
	 */
	public void setStat(StatEnum value) {
		this.stat = value;
	}

	/**
	 * Gets the value of the func property.
	 * 
	 * @return possible object is {@link Func }
	 */
	public Func getFunc() {
		return func;
	}

	/**
	 * Sets the value of the func property.
	 * 
	 * @param value
	 *          allowed object is {@link Func }
	 */
	public void setFunc(Func value) {
		this.func = value;
	}

	/**
	 * Gets the value of the value property.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Sets the value of the value property.
	 */
	public void setValue(int value) {
		this.value = value;
	}

}
