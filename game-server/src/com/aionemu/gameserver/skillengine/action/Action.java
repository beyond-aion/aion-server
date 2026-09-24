package com.aionemu.gameserver.skillengine.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.effect.modifier.ActionModifiers;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Action")
public abstract class Action {

	protected ActionModifiers modifiers;

	/**
	 * Perform action specified in template
	 */
	public abstract boolean act(Skill skill);

	/**
	 * Checks whether {@link #act(Skill)} could be performed, without performing it.
	 *
	 * @return True, if the action can be performed
	 */
	public boolean canAct(Skill skill) {
		return true;
	}

}
