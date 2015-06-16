package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillAttackInstantEffect")
public class SkillAttackInstantEffect extends DamageEffect {

	@XmlAttribute
	protected int rnddmg;//TODO should be enum and different types of random damage behaviour
	@XmlAttribute
	protected boolean cannotmiss;

	/**
	 * @return the rnddmg
	 */
	public int getRnddmg() {
		return rnddmg;
	}

	/**
	 * @return the cannotmiss
	 */
	public boolean isCannotmiss() {
		return cannotmiss;
	}
	
	
}
