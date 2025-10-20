package com.aionemu.gameserver.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

	private final List<ActionObserver> observers = new ArrayList<>();
	private final List<AttackCalcObserver> attackCalcObservers = new CopyOnWriteArrayList<>();

	/**
	 * Adds the observer for a single notification. It will be automatically removed from this controller after receiving the notification.
	 */
	public void attach(ActionObserver observer) {
		observer.makeOneTimeUse();
		addObserver(observer);
	}

	public void addObserver(ActionObserver observer) {
		synchronized (observers) {
			observers.add(observer);
		}
	}

	public void addAttackCalcObserver(AttackCalcObserver observer) {
		attackCalcObservers.add(observer);
	}

	public void removeObserver(ActionObserver observer) {
		boolean removed;
		synchronized (observers) {
			removed = observers.remove(observer);
		}
		if (removed)
			observer.onRemoved();
	}

	public void removeAttackCalcObserver(AttackCalcObserver observer) {
		attackCalcObservers.remove(observer);
	}

	public void notifyObservers(ObserverType type, Object... object) {
		List<ActionObserver> notifiable = Collections.emptyList();
		synchronized (observers) {
			if (observers.isEmpty())
				return;
			for (Iterator<ActionObserver> iterator = observers.iterator(); iterator.hasNext(); ) {
				ActionObserver observer = iterator.next();
				if (observer.getObserverType().matchesObserver(type)) {
					if (notifiable.isEmpty())
						notifiable = new ArrayList<>();
					notifiable.add(observer);
					if (observer.isOneTimeUse())
						iterator.remove();
				}
			}
		}

		// notify outside of lock
		for (ActionObserver observer : notifiable) {
			notifyAction(type, observer, object);
			if (observer.isOneTimeUse())
				observer.onRemoved();
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

	public void notifyDeathObservers(Creature lastAttacker) {
		notifyObservers(ObserverType.DEATH, lastAttacker);
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

	public void clear() {
		List<ActionObserver> removed;
		synchronized (observers) {
			removed = new ArrayList<>(observers);
			observers.clear();
		}
		removed.forEach(ActionObserver::onRemoved);
		attackCalcObservers.clear();
	}
}
