package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author MrPoke
 */
public abstract class ItemUseObserver extends ActionObserver {

	public ItemUseObserver() {
		super(ObserverType.ALL);
	}

	@Override
	public final void attack(Creature creature, int skillId) {
		abort();
	}

	@Override
	public final void attacked(Creature creature, int skillId) {
		abort();
	}

	@Override
	public final void died(Creature creature) {
		abort();
	}

	@Override
	public final void dotattacked(Creature creature, Effect dotEffect) {
		abort();
	}

	@Override
	public final void equip(Item item, Player owner) {
		abort();
	}

	@Override
	public final void unequip(Item item, Player owner) {
		abort();
	}

	@Override
	public final void moved() {
		abort();
	}

	@Override
	public final void startSkillCast(Skill skill) {
		abort();
	}

	@Override
	public final void sit() {
		abort();
	}

	@Override
	public void endSkillCast(Skill skill) {
		abort();
	}

	@Override
	public void itemused(Item item) {
		abort();
	}

	@Override
	public void boostSkillCost(Skill skill) {
		abort();
	}

	public abstract void abort();
}
