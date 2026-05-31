package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillAttackInstantEffect")
public class SkillAttackInstantEffect extends DamageEffect {

	@XmlAttribute
	protected int rnddmg;// TODO should be enum and different types of random damage behaviour
	@XmlAttribute
	protected boolean cannotmiss;

	public int getRnddmg() {
		return rnddmg;
	}

	public boolean isCannotmiss() {
		return cannotmiss;
	}

	@Override
	public boolean isNoResist() {
		return cannotmiss || super.isNoResist();
	}

}
