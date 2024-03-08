package com.aionemu.gameserver.controllers.observer;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
public class ActionObserver {

	private AtomicBoolean used;

	private final ObserverType observerType;

	public ActionObserver(ObserverType observerType) {
		this.observerType = observerType;
	}

	public void makeOneTimeUse() {
		used = new AtomicBoolean(false);
	}

	public boolean tryUse() {
		return used.compareAndSet(false, true);
	}

	public ObserverType getObserverType() {
		return observerType;
	}

	public void moved() {
	}

	/**
	 * @param creature who effected
	 * @param skillId - effector skill id, which called this method
	 */
	public void attacked(Creature creature, int skillId) {
	}

	public void attack(Creature creature, int skillId) {
	}

	public void equip(Item item, Player owner) {
	}

	public void unequip(Item item, Player owner) {
	}

	public void startSkillCast(Skill skill) {
	}

	public void endSkillCast(Skill skill) {
	}

	public void boostSkillCost(Skill skill) {
	}

	public void died(Creature creature) {
	}

	public void dotattacked(Creature creature, Effect dotEffect) {
	}

	public void itemused(Item item) {
	}

	public void abnormalsetted(AbnormalState state) {
	}

	public void summonrelease() {
	}

	public void sit() {
	}

	public void hpChanged(int value) {
	}
}
