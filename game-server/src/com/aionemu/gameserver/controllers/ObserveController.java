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
 * Notes:<br>
 * 1) There should be locking against onceUsedObservers<br>
 * 2) Check observers size before iteration to minimize memory allocations
 * 
 * @author ATracer
 * @author Cura
 */
public class ObserveController {

	private ReentrantLock lock = new ReentrantLock();
	protected Collection<ActionObserver> observers = new CopyOnWriteArrayList<>();
	protected List<ActionObserver> onceUsedObservers = new ArrayList<>();
	protected Collection<AttackCalcObserver> attackCalcObservers = new CopyOnWriteArrayList<>();

	/**
	 * Once used observer add to observerController. If observer notify will be removed.
	 * 
	 * @param observer
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

	/**
	 * @param observer
	 */
	public void addObserver(ActionObserver observer) {
		observers.add(observer);
	}

	/**
	 * @param observer
	 */
	public void addAttackCalcObserver(AttackCalcObserver observer) {
		attackCalcObservers.add(observer);
	}

	/**
	 * @param observer
	 */
	public void removeObserver(ActionObserver observer) {
		observers.remove(observer);
		lock.lock();
		try {
			onceUsedObservers.remove(observer);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @param observer
	 */
	public void removeAttackCalcObserver(AttackCalcObserver observer) {
		attackCalcObservers.remove(observer);
	}

	/**
	 * notify all observers
	 */
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

	/**
	 * @param notify
	 *          that creature died
	 */
	public void notifyDeathObservers(Creature creature) {
		notifyObservers(ObserverType.DEATH, creature);
	}

	/**
	 * notify that creature moved
	 */
	public void notifyMoveObservers() {
		notifyObservers(ObserverType.MOVE);
	}

	/**
	 * notify that creature moved
	 */
	public void notifySitObservers() {
		notifyObservers(ObserverType.SIT);
	}

	/**
	 * notify that creature attacking
	 *
	 * @param damage
	 * @param skillId
	 */
	public void notifyAttackObservers(Creature creature, int skillId) {
		notifyObservers(ObserverType.ATTACK, creature, skillId);
	}

	/**
	 * notify that creature attacked
	 */
	public void notifyAttackedObservers(Creature creature, int skillId) {
		notifyObservers(ObserverType.ATTACKED, creature, skillId);
	}

	/**
	 * notify that creature attacked by dot's hit
	 */
	public void notifyDotAttackedObservers(Creature creature, Effect effect) {
		notifyObservers(ObserverType.DOT_ATTACKED, creature, effect);
	}

	/**
	 * notify that creature used a skill
	 */
	public void notifyStartSkillCastObservers(Skill skill) {
		notifyObservers(ObserverType.STARTSKILLCAST, skill);
	}

	public void notifyEndSkillCastObservers(Skill skill) {
		notifyObservers(ObserverType.ENDSKILLCAST, skill);
	}

	public void notifyBoostSkillCostObservers(Skill skill) {
		notifyObservers(ObserverType.BOOSTSKILLCOST, skill);
	}

	/**
	 * @param item
	 * @param owner
	 */
	public void notifyItemEquip(Item item, Player owner) {
		notifyObservers(ObserverType.EQUIP, item, owner);
	}

	/**
	 * @param item
	 * @param owner
	 */
	public void notifyItemUnEquip(Item item, Player owner) {
		notifyObservers(ObserverType.UNEQUIP, item, owner);
	}

	/**
	 * notify that player used an item
	 */
	public void notifyItemuseObservers(Item item) {
		notifyObservers(ObserverType.ITEMUSE, item);
	}

	/**
	 * notify that abnormalstate is setted in effectcontroller
	 */
	public void notifyAbnormalSettedObservers(AbnormalState state) {
		notifyObservers(ObserverType.ABNORMALSETTED, state);
	}

	/**
	 * notify that abnormalstate is setted in effectcontroller
	 */
	public void notifySummonReleaseObservers() {
		notifyObservers(ObserverType.SUMMONRELEASE);
	}

	public void notifyHPChangeObservers(int hpValue) {
		notifyObservers(ObserverType.HP_CHANGED, hpValue);
	}

	/**
	 * @param status
	 * @return true or false
	 */
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

	/**
	 * @param status
	 * @return
	 */
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
