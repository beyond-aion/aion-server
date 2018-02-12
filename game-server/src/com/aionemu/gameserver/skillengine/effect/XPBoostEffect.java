package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author antness
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XPBoostEffect")
public class XPBoostEffect extends BufEffect {

	@Override
	public void calculate(Effect effect) {
		effect.addSuccessEffect(this);
	}

}
