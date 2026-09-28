package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author ATracer, Neon
 */
@XmlEnum
public enum AbnormalState {
	NONE(0),
	POISON(1 << 0),
	BLEED(1 << 1),
	PARALYZE(1 << 2),
	SLEEP(1 << 3),
	ROOT(1 << 4),
	BLIND(1 << 5),
	CHARM(1 << 6),
	DISEASE(1 << 7),
	SILENCE(1 << 8),
	FEAR(1 << 9),
	CURSE(1 << 10),
	CONFUSE(1 << 11),
	STUN(1 << 12),
	PETRIFICATION(1 << 13),
	STUMBLE(1 << 14),
	STAGGER(1 << 15), // knockback
	OPENAERIAL(1 << 16),
	SNARE(1 << 17),
	SLOW(1 << 18),
	SPIN(1 << 19),
	BIND(1 << 20),
	DEFORM(1 << 21),
	PULLED(1 << 22),
	NOFLY(1 << 23),
	SIMPLE_MOVE_BACK(1 << 24),
	STUNLIKE(1 << 25),
	CANT_MOVE_OR_ATTACK(1 << 26),
	UNK(1 << 27),
	UNK_2(1 << 28),
	HIDE(1 << 29),
	INVULNERABLE_WING(1 << 30),
	SANCTUARY(1 << 31),

	/**
	 * Compound abnormal states
	 */
	ANY_STUN(STUN.id | STUMBLE.id | STAGGER.id | SPIN.id),
	CANT_ATTACK_STATE(ANY_STUN.id | PARALYZE.id | SLEEP.id | FEAR.id | OPENAERIAL.id | PULLED.id | SANCTUARY.id | CONFUSE.id),
	STANCE_OFF(ANY_STUN.id | PARALYZE.id | FEAR.id | OPENAERIAL.id | PULLED.id | SANCTUARY.id | CONFUSE.id),
	CANT_MOVE_STATE(ANY_STUN.id | PARALYZE.id | SLEEP.id | ROOT.id | OPENAERIAL.id | PULLED.id | SANCTUARY.id),
	DISMOUNT_RIDE(ANY_STUN.id | PARALYZE.id | SLEEP.id | ROOT.id | OPENAERIAL.id | PULLED.id | FEAR.id | SNARE.id | DEFORM.id | CONFUSE.id),
	AUTOMATICALLY_STANDUP(PARALYZE.id | SLEEP.id | FEAR.id | STUN.id | STAGGER.id | OPENAERIAL.id | SPIN.id | DEFORM.id | PULLED.id | CONFUSE.id),
	CANCEL_ITEM_USE(ANY_STUN.id | PARALYZE.id | SLEEP.id | CHARM.id | FEAR.id | CONFUSE.id | PETRIFICATION.id | OPENAERIAL.id | PULLED.id);

	private final int id;

	AbnormalState(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
