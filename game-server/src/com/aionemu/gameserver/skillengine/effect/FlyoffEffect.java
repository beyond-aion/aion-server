package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlyOffEffect")
public class FlyoffEffect extends EffectTemplate {

	@XmlAttribute
	protected int distance;

	@Override
	public void applyEffect(Effect effect) {
		// TODO Distance is Z, value probably contains angle or width
	}

}
