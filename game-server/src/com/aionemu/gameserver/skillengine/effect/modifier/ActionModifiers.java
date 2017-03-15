package com.aionemu.gameserver.skillengine.effect.modifier;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActionModifiers")
public class ActionModifiers {

	@XmlElements({ @XmlElement(name = "frontdamage", type = FrontDamageModifier.class),
		@XmlElement(name = "backdamage", type = BackDamageModifier.class), @XmlElement(name = "abnormaldamage", type = AbnormalDamageModifier.class),
		@XmlElement(name = "targetrace", type = TargetRaceDamageModifier.class),
		@XmlElement(name = "targetclass", type = TargetClassDamageModifier.class) })
	protected List<ActionModifier> actionModifiers;

	/**
	 * Gets the value of the actionModifiers property.
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link StumbleDamageModifier } {@link FrontDamageModifier } {@link BackDamageModifier }
	 * {@link StunDamageModifier } {@link PoisonDamageModifier } {@link TargetRaceDamageModifier }
	 */
	public List<ActionModifier> getActionModifiers() {
		if (actionModifiers == null) {
			actionModifiers = new ArrayList<>();
		}
		return this.actionModifiers;
	}
}
