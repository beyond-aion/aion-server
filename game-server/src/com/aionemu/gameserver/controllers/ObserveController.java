package com.aionemu.gameserver.controllers;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.observer.*;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.ShieldType;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer, Cura
 */
public class ObserveController {

	private ReentrantLock lock = new ReentrantLock();
	protected Collection<ActionObserver> observers = new CopyOnWriteArrayList<>();
	protected List<ActionObserver> onceUsedObservers = new ArrayList<>();
	protected Collection<AttackCalcObserver> attackCalcObservers = new CopyOnWriteArrayList<>();

	/**
	 * Once used observer add to observerController. If observer notify will be removed.
	 */
	public void attach(ActionObserver observer) {
		observer.makeOneTimeUse();
		lock.lock();
		try {
			onceUsedObservers.add(observer);
		} finally {
			lock.unlock();
		}
	}

	public void addObserver(ActionObserver observer) {
		observers.add(observer);
	}

	public void addAttackCalcObserver(AttackCalcObserver observer) {
		attackCalcObservers.add(observer);
	}

	public void removeObserver(ActionObserver observer) {
		observers.remove(observer);
		lock.lock();
		try {
			onceUsedObservers.remove(observer);
		} finally {
			lock.unlock();
		}
	}

	public void removeAttackCalcObserver(AttackCalcObserver observer) {
		attackCalcObservers.remove(observer);
	}

	public void notifyObservers(ObserverType type, Object... object) {
		List<ActionObserver> tempOnceused = Collections.emptyList();
		lock.lock();
		try {
			if (onceUsedObservers.size() > 0) {
				tempOnceused = new ArrayList<>();
				Iterator<ActionObserver> iterator = onceUsedObservers.iterator();
				while (iterator.hasNext()) {
					ActionObserver observer = iterator.next();
					if (observer.getObserverType().matchesObserver(type)) {
						if (observer.tryUse()) {
							tempOnceused.add(observer);
							iterator.remove();
						}
					}
				}
			}
		} finally {
			lock.unlock();
		}

		// notify outside of lock
		for (ActionObserver observer : tempOnceused) {
			notifyAction(type, observer, object);
		}

		if (observers.size() > 0) {
			for (ActionObserver observer : observers) {
				if (observer.getObserverType().matchesObserver(type)) {
					notifyAction(type, observer, object);
				}
			}
		}
	}

	private void notifyAction(ObserverType type, ActionObserver observer, Object... object) {
		switch (type) {
			case ATTACK:
				observer.attack((Creature) object[0], (int) object[1]);
				break;
			case ATTACKED:
				observer.attacked((Creature) object[0], (int) object[1]);
				break;
			case DEATH:
				observer.died((Creature) object[0]);
				break;
			case EQUIP:
				observer.equip((Item) object[0], (Player) object[1]);
				break;
			case UNEQUIP:
				observer.unequip((Item) object[0], (Player) object[1]);
				break;
			case MOVE:
				observer.moved();
				break;
			case STARTSKILLCAST:
				observer.startSkillCast((Skill) object[0]);
				break;
			case ENDSKILLCAST:
				observer.endSkillCast((Skill) object[0]);
				break;
			case BOOSTSKILLCOST:
				observer.boostSkillCost((Skill) object[0]);
				break;
			case DOT_ATTACKED:
				observer.dotattacked((Creature) object[0], (Effect) object[1]);
				break;
			case ITEMUSE:
				observer.itemused((Item) object[0]);
				break;
			case ABNORMALSETTED:
				observer.abnormalsetted((AbnormalState) object[0]);
				break;
			case SUMMONRELEASE:
				observer.summonrelease();
				break;
			case SIT:
				observer.sit();
				break;
			case HP_CHANGED:
				observer.hpChanged((int) object[0]);
				break;
		}
	}

	public void notifyDeathObservers(Creature creature) {
		notifyObservers(ObserverType.DEATH, creature);
	}

	public void notifyMoveObservers() {
		notifyObservers(ObserverType.MOVE);
	}

	public void notifySitObservers() {
		notifyObservers(ObserverType.SIT);
	}

	public void notifyAttackObservers(Creature creature, int skillId) {
		notifyObservers(ObserverType.ATTACK, creature, skillId);
	}

	public void notifyAttackedObservers(Creature creature, int skillId) {
		notifyObservers(ObserverType.ATTACKED, creature, skillId);
	}

	public void notifyDotAttackedObservers(Creature creature, Effect effect) {
		notifyObservers(ObserverType.DOT_ATTACKED, creature, effect);
	}

	public void notifyStartSkillCastObservers(Skill skill) {
		notifyObservers(ObserverType.STARTSKILLCAST, skill);
	}

	public void notifyEndSkillCastObservers(Skill skill) {
		notifyObservers(ObserverType.ENDSKILLCAST, skill);
	}

	public void notifyBoostSkillCostObservers(Skill skill) {
		notifyObservers(ObserverType.BOOSTSKILLCOST, skill);
	}

	public void notifyItemEquip(Item item, Player owner) {
		notifyObservers(ObserverType.EQUIP, item, owner);
	}

	public void notifyItemUnEquip(Item item, Player owner) {
		notifyObservers(ObserverType.UNEQUIP, item, owner);
	}

	public void notifyItemuseObservers(Item item) {
		notifyObservers(ObserverType.ITEMUSE, item);
	}

	public void notifyAbnormalSettedObservers(AbnormalState state) {
		notifyObservers(ObserverType.ABNORMALSETTED, state);
	}

	public void notifySummonReleaseObservers() {
		notifyObservers(ObserverType.SUMMONRELEASE);
	}

	public void notifyHPChangeObservers(int hpValue) {
		notifyObservers(ObserverType.HP_CHANGED, hpValue);
	}

	public boolean checkAttackStatus(AttackStatus status) {
		if (attackCalcObservers.size() > 0) {
			for (AttackCalcObserver observer : attackCalcObservers) {
				if (observer.checkStatus(status)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkAttackerStatus(AttackStatus status) {
		if (attackCalcObservers.size() > 0) {
			for (AttackCalcObserver observer : attackCalcObservers) {
				if (observer.checkAttackerStatus(status)) {
					return true;
				}
			}
		}
		return false;
	}

	public AttackerCriticalStatus checkAttackerCriticalStatus(AttackStatus status, boolean isSkill) {
		if (attackCalcObservers.size() > 0) {
			for (AttackCalcObserver observer : attackCalcObservers) {
				AttackerCriticalStatus acStatus = observer.checkAttackerCriticalStatus(status, isSkill);
				if (acStatus.isResult()) {
					return acStatus;
				}
			}
		}
		return new AttackerCriticalStatus(false);
	}

	public void checkShieldStatus(List<AttackResult> attackList, Effect effect, Creature attacker) {
		checkShieldStatus(attackList, effect, attacker, null);
	}

	public void checkShieldStatus(List<AttackResult> attackList, Effect effect, Creature attacker, ShieldType shieldType) {
		if (attackCalcObservers.size() > 0) {
			for (AttackCalcObserver observer : attackCalcObservers) {
				if (shieldType == null || observer instanceof AttackShieldObserver && ((AttackShieldObserver) observer).getShieldType() == shieldType)
					observer.checkShield(attackList, effect, attacker);
			}
		}
	}

	public float getBasePhysicalDamageMultiplier(boolean isSkill) {
		float multiplier = 1;
		if (attackCalcObservers.size() > 0) {
			for (AttackCalcObserver observer : attackCalcObservers) {
				multiplier *= observer.getBasePhysicalDamageMultiplier(isSkill);
			}
		}
		return multiplier;
	}

	public float getBaseMagicalDamageMultiplier() {
		float multiplier = 1;
		if (attackCalcObservers.size() > 0) {
			for (AttackCalcObserver observer : attackCalcObservers) {
				multiplier *= observer.getBaseMagicalDamageMultiplier();
			}
		}
		return multiplier;
	}

	/**
	 * Clear all observers
	 */
	public void clear() {
		lock.lock();
		try {
			onceUsedObservers.clear();
		} finally {
			lock.unlock();
		}
		observers.clear();
		attackCalcObservers.clear();
	}
}
