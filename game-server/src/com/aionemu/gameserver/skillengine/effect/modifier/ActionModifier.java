package com.aionemu.gameserver.skillengine.effect.modifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.change.Func;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActionModifier")
public abstract class ActionModifier {

	@XmlAttribute
	protected int delta;
	@XmlAttribute(required = true)
	protected int value;
	@XmlAttribute
	protected Func mode = Func.ADD;

	/**
	 * Applies modifier to original value
	 * 
	 * @param effect
	 * @param originalValue
	 * @return int
	 */
	public abstract int analyze(Effect effect);

	/**
	 * Performs check of condition
	 * 
	 * @param effect
	 * @return true or false
	 */
	public abstract boolean check(Effect effect);

	public Func getFunc() {
		return mode;
	}
}
