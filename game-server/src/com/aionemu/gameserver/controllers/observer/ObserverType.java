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
	NPCDIALOGREQUEST(1 << 9),
	ABNORMALSETTED(1 << 10),
	SUMMONRELEASE(1 << 11),
	SIT(1 << 12),
	HP_CHANGED(1 << 13),
	ENDSKILLCAST(1 << 14),
	BOOSTSKILLCOST(1 << 15),
	GODSTONE(1 << 16),
	EQUIP_UNEQUIP(EQUIP.observerMask | UNEQUIP.observerMask),
	ATTACK_DEFEND(ATTACK.observerMask | ATTACKED.observerMask),
	MOVE_OR_DIE(MOVE.observerMask | DEATH.observerMask),
	ALL(MOVE.observerMask | ATTACK.observerMask | ATTACKED.observerMask | EQUIP.observerMask | UNEQUIP.observerMask | STARTSKILLCAST.observerMask
		| DEATH.observerMask | DOT_ATTACKED.observerMask | ITEMUSE.observerMask | NPCDIALOGREQUEST.observerMask | ABNORMALSETTED.observerMask
		| SUMMONRELEASE.observerMask | SIT.observerMask | HP_CHANGED.observerMask | ENDSKILLCAST.observerMask | BOOSTSKILLCOST.observerMask
		| GODSTONE.observerMask);

	private int observerMask;

	private ObserverType(int observerMask) {
		this.observerMask = observerMask;
	}

	public boolean matchesObserver(ObserverType observerType) {
		return (observerType.observerMask & observerMask) == observerType.observerMask;
	}
}
