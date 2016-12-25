package com.aionemu.gameserver.skillengine.effect;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.aionemu.gameserver.skillengine.model.DispelSlotType;
import com.aionemu.gameserver.skillengine.model.DispelType;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
public class DispelEffect extends EffectTemplate {

	@XmlElement
	private List<Integer> effectids;
	@XmlElement
	private List<EffectType> effecttype;
	@XmlElement
	private List<DispelSlotType> slottype;
	@XmlAttribute
	private DispelType dispeltype;
	@XmlAttribute
	private int count;
	@XmlAttribute
	private int dpower;
	@XmlAttribute
	private int power = 100;
	@XmlAttribute(name = "dispel_level")
	private int dispelLevel = 100;

	@Override
	public void applyEffect(Effect effect) {
		if (effect.getEffected() == null || effect.getEffected().getEffectController() == null)
			return;

		if (dispeltype == null)
			return;

		if ((dispeltype == DispelType.EFFECTID || dispeltype == DispelType.EFFECTIDRANGE) && effectids == null)
			return;

		if (dispeltype == DispelType.EFFECTTYPE && effecttype == null)
			return;

		if (dispeltype == DispelType.SLOTTYPE && slottype == null)
			return;

		int finalPower = power + dpower * effect.getSkillLevel();

		switch (dispeltype) {
			case EFFECTID:
				int removedEffects = 0;
				for (Integer effectId : effectids) {
					if (removedEffects == count)
						break;
					if (effect.getEffected().getEffectController().removeByEffectId(effectId, dispelLevel, finalPower))
						removedEffects++;
				}
				break;
			case EFFECTIDRANGE:
				int removedEffectCount = 0;
				for (int effectId = effectids.get(0); effectId <= effectids.get(1); effectId++) {
					if (removedEffectCount == count)
						break;
					if (effect.getEffected().getEffectController().removeByEffectId(effectId, dispelLevel, finalPower))
						removedEffectCount++;
				}
				break;
			case EFFECTTYPE:
				for (EffectType type : effecttype) {
					effect.getEffected().getEffectController().removeByDispelEffect(type, null, count, dispelLevel, finalPower);
				}
				break;
			case SLOTTYPE:
				for (DispelSlotType type : slottype) {
					effect.getEffected().getEffectController().removeByDispelEffect(null, type, count, dispelLevel, finalPower);
				}
				break;
		}
	}
}
