package com.aionemu.gameserver.skillengine.effect.modifier;

import java.util.List;

import jakarta.xml.bind.annotation.*;

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

	public List<ActionModifier> getActionModifiers() {
		return actionModifiers == null ? List.of() : actionModifiers;
	}
}
