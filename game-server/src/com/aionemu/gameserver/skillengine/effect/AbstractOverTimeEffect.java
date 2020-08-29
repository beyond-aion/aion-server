package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractOverTimeEffect")
public abstract class AbstractOverTimeEffect extends EffectTemplate {

	@XmlAttribute(required = true)
	protected int checktime;
	@XmlAttribute
	protected boolean percent;
	@XmlAttribute
	protected boolean shared;

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(Effect effect) {
		this.startEffect(effect, null);
	}

	public void startEffect(final Effect effect, AbnormalState abnormal) {
		final Creature effected = effect.getEffected();
		// Some skills have a duration2 of 2000 and a checktime of 1000. (e.g. Ripple of Purification)
		// On retail these skills are only applied once (instead of 2x).
		// We add 300ms to the waitingTime to prevent them from being applied twice.
		long waitingTime = 300 + (checktime > getDuration2() ? checktime - getDuration2() : checktime);

		if (abnormal != null) {
			effect.setAbnormal(abnormal);
			effected.getEffectController().setAbnormal(abnormal);
		}
		// TODO figure out what to do with such cases
		if (checktime == 0)
			return;
		Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> onPeriodicAction(effect), waitingTime, checktime);
		effect.setPeriodicTask(task, position);
	}

	public void endEffect(Effect effect, AbnormalState abnormal) {
		if (abnormal != null)
			effect.getEffected().getEffectController().unsetAbnormal(abnormal);
	}

	@Override
	public int getDuration2() {
		return duration2 + 1000; // on retail these effects last one sec more than their template value of duration2
	}
}
