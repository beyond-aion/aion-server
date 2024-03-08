package com.aionemu.gameserver.controllers.observer;

/**
 * @author ATracer
 */
public enum ObserverType {
	MOVE(1),
	ATTACK(1 << 1),
	ATTACKED(1 << 2),
	EQUIP(1 << 3),
	UNEQUIP(1 << 4),
	STARTSKILLCAST(1 << 5),
	DEATH(1 << 6),
	DOT_ATTACKED(1 << 7),
	ITEMUSE(1 << 8),
	ABNORMALSETTED(1 << 9),
	SUMMONRELEASE(1 << 10),
	SIT(1 << 11),
	HP_CHANGED(1 << 12),
	ENDSKILLCAST(1 << 13),
	BOOSTSKILLCOST(1 << 14),
	EQUIP_UNEQUIP(EQUIP.observerMask | UNEQUIP.observerMask),
	ATTACK_DEFEND(ATTACK.observerMask | ATTACKED.observerMask),
	DOT_ATTACK_DEFEND(DOT_ATTACKED.observerMask | ATTACK.observerMask | ATTACKED.observerMask),
	MOVE_OR_DIE(MOVE.observerMask | DEATH.observerMask),
	ALL(MOVE.observerMask | ATTACK.observerMask | ATTACKED.observerMask | EQUIP.observerMask | UNEQUIP.observerMask | STARTSKILLCAST.observerMask
		| DEATH.observerMask | DOT_ATTACKED.observerMask | ITEMUSE.observerMask | ABNORMALSETTED.observerMask | SUMMONRELEASE.observerMask
		| SIT.observerMask | HP_CHANGED.observerMask | ENDSKILLCAST.observerMask | BOOSTSKILLCOST.observerMask);

	private int observerMask;

	private ObserverType(int observerMask) {
		this.observerMask = observerMask;
	}

	public boolean matchesObserver(ObserverType observerType) {
		return (observerType.observerMask & observerMask) == observerType.observerMask;
	}
}
