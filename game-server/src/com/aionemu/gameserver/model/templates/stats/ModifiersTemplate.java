package com.aionemu.gameserver.model.templates.stats;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.stats.calc.functions.StatAbsFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatRateFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatSetFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatSubFunction;

/**
 * @author xavier, Rolandas
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "modifiers")
public class ModifiersTemplate {

	@XmlElements({ @XmlElement(name = "sub", type = StatSubFunction.class), @XmlElement(name = "add", type = StatAddFunction.class),
		@XmlElement(name = "rate", type = StatRateFunction.class), @XmlElement(name = "set", type = StatSetFunction.class),
		@XmlElement(name = "abs", type = StatAbsFunction.class) })
	private List<StatFunction> modifiers;

	@XmlAttribute
	private float chance = 100f;

	public List<StatFunction> getModifiers() {
		return modifiers;
	}

	/**
	 * @return the chance
	 */
	public float getChance() {
		return chance;
	}

}
