package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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

	public void startEffect(Effect effect, AbnormalState abnormal) {
		Creature effected = effect.getEffected();
		if (abnormal != null) {
			effect.setAbnormal(abnormal);
			effected.getEffectController().setAbnormal(abnormal);
		}
		// TODO figure out what to do with such cases
		if (checktime == 0)
			return;
		// effects without a duration (like toggle skills) tick until they get removed
		AtomicInteger remainingTicks = new AtomicInteger(effect.getDuration() > 0 ? getTickCount(effect.getDuration()) : Integer.MAX_VALUE);
		Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (remainingTicks.getAndDecrement() > 0)
				onPeriodicAction(effect);
		}, checktime, checktime);
		effect.setPeriodicTask(task, position);
	}

	public void endEffect(Effect effect, AbnormalState abnormal) {
		if (abnormal != null)
			effect.getEffected().getEffectController().unsetAbnormal(abnormal);
	}

	public int getChecktime() {
		return checktime;
	}

	/**
	 * Rounds the duration down to a whole number of ticks and adds one second on top. Ticking runs while more than one interval is left, so this is what
	 * decides the tick count: a duration of 10000 with a checktime of 1000 yields ten ticks, and one of 1000 yields a single one.
	 *
	 * @param duration The duration after all multipliers have been applied.
	 */
	public long roundDurationToTicks(long duration) {
		if (duration <= 0 || checktime <= 0)
			return duration;
		return duration + 1000 - duration % checktime;
	}

	/**
	 * @return The number of ticks over the given duration: one per interval, minus the last one, since the effect ends before it can run.
	 */
	public int getTickCount(int duration) {
		return duration <= 0 || checktime <= 0 ? 0 : (duration - 1) / checktime;
	}
}
