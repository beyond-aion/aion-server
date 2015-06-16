package com.aionemu.gameserver.model.templates.itemset;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "FullBonus")
@XmlAccessorType(XmlAccessType.FIELD)
public class FullBonus {

	@XmlElement(name = "modifiers", required = false)
	protected ModifiersTemplate modifiers;

	private int totalnumberofitems;

	public List<StatFunction> getModifiers() {
		return modifiers != null ? modifiers.getModifiers() : null;
	}

	/**
	 * @return Value of the number of items in the set
	 */
	public int getCount() {
		return totalnumberofitems;
	}

	/**
	 * Sets number of items in the set (when this bonus applies)
	 * 
	 * @param number
	 */
	public void setNumberOfItems(int number) {
		this.totalnumberofitems = number;
	}
}
