package com.aionemu.gameserver.skillengine.effect;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.aionemu.gameserver.skillengine.model.DispelType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;

/**
 * @author ATracer
 */
public class DispelEffect extends EffectTemplate {

	@XmlElement(type = Integer.class)
	protected List<Integer> effectids;
	@XmlElement
	protected List<String> effecttype;
	@XmlElement
	protected List<String> slottype;
	@XmlAttribute
	protected DispelType dispeltype;
	@XmlAttribute
	protected int count;
	@XmlAttribute
	protected int dpower;
	@XmlAttribute
	protected int power = 100;
	@XmlAttribute(name = "dispel_level")
	protected int dispelLevel = 100;

	@Override
	public void applyEffect(Effect effect) {
		if (effect.getEffected() == null || effect.getEffected().getEffectController() == null)
			return;

		if (dispeltype == null)
			return;

		if ((dispeltype == DispelType.EFFECTID || dispeltype == DispelType.EFFECTIDRANGE) && 
			effectids == null)
			return;

		if (dispeltype == DispelType.EFFECTTYPE && effecttype == null)
			return;
		
		if (dispeltype == DispelType.SLOTTYPE && slottype == null)
			return;

		int finalPower = power + dpower * effect.getSkillLevel();
		
		switch (dispeltype) {
			case EFFECTID:
				for (Integer effectId : effectids) {
					effect.getEffected().getEffectController().removeByDispelEffect(dispeltype, effectId.toString(), this.count, this.dispelLevel, finalPower);
				}
				break;
			case EFFECTIDRANGE:
				for (int i = effectids.get(0); i <= effectids.get(1); i++) {
					effect.getEffected().getEffectController().removeByDispelEffect(dispeltype, Integer.toString(i), this.count, this.dispelLevel, finalPower);
				}
				break;
			case EFFECTTYPE:
				for (String type : effecttype) {
					EffectType temp = null;
					try {
						temp = EffectType.valueOf(type);
					} catch (Exception e) {
						log.error("wrong effecttype in dispeleffect "+type);
					}
					if (temp != null)
						effect.getEffected().getEffectController().removeByDispelEffect(dispeltype, temp.toString(), this.count, this.dispelLevel, finalPower);
				}
				break;
			case SLOTTYPE:
				for (String type : slottype) {
					SkillTargetSlot temp = null;
					try {
						temp = SkillTargetSlot.valueOf(type);
					} catch (Exception e) {
						log.error("wrong slottype in dispeleffect "+type);
					}
					if (temp != null)
						effect.getEffected().getEffectController().removeByDispelEffect(dispeltype, temp.toString(), this.count, this.dispelLevel, finalPower);
				}
				break;
		}
	}
}
