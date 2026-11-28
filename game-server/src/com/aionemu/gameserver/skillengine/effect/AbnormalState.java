package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlEnum;

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
	BUFF_STUN(1 << 25),
	BUFF_SLEEP(1 << 26),
	BUFF_SILENCE(1 << 27),
	BUFF_BIND(1 << 28),
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
	CANCEL_ITEM_USE(ANY_STUN.id | PARALYZE.id | SLEEP.id | CHARM.id | FEAR.id | CONFUSE.id | PETRIFICATION.id | OPENAERIAL.id | PULLED.id),
	// npcs carry on with their fight when confused
	NPC_CANT_ATTACK_STATE(CANT_ATTACK_STATE.id & ~CONFUSE.id),
	// the states which hold a creature in place, whatever else it is under
	HARD_CC(PARALYZE.id | SLEEP.id | ROOT.id | CHARM.id | STUN.id | PETRIFICATION.id | STUMBLE.id | STAGGER.id | OPENAERIAL.id | SPIN.id | PULLED.id
		| BUFF_STUN.id | BUFF_SLEEP.id),
	// suspends server driven movement like fear or confuse, without ending it
	CANT_BE_FORCE_MOVED(HARD_CC.id | SIMPLE_MOVE_BACK.id),
	// ends a glide and does not let one start
	CANT_GLIDE(HARD_CC.id | SIMPLE_MOVE_BACK.id | FEAR.id | CONFUSE.id | SNARE.id);

	private final int id;

	AbnormalState(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
