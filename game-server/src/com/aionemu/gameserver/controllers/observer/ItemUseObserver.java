package com.aionemu.gameserver.controllers.observer;

import static com.aionemu.gameserver.controllers.observer.ObserverType.*;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author MrPoke
 */
public abstract class ItemUseObserver extends ActionObserver {

	private final Player observed;
	private final AtomicBoolean aborted = new AtomicBoolean();

	public ItemUseObserver(Player observed) {
		super(ATTACK, ATTACKED, DEATH, DOT_ATTACKED, EQUIP, UNEQUIP, MOVE, STARTSKILLCAST, ENDSKILLCAST, SIT, ITEMUSE, ABNORMALSETTED, BOOSTSKILLCOST);
		this.observed = observed;
	}

	@Override
	public final void attack(Creature creature, int skillId) {
		tryAbort();
	}

	@Override
	public final void attacked(Creature creature, int skillId) {
		tryAbort();
	}

	@Override
	public final void died(Creature creature) {
		tryAbort();
	}

	@Override
	public final void dotattacked(Creature creature, Effect dotEffect) {
		tryAbort();
	}

	@Override
	public final void equip(Item item, Player owner) {
		tryAbort();
	}

	@Override
	public final void unequip(Item item, Player owner) {
		tryAbort();
	}

	@Override
	public final void moved() {
		tryAbort();
	}

	@Override
	public final void startSkillCast(Skill skill) {
		tryAbort();
	}

	@Override
	public final void sit() {
		tryAbort();
	}

	@Override
	public void endSkillCast(Skill skill) {
		tryAbort();
	}

	@Override
	public void itemused(Item item) {
		tryAbort();
	}

	@Override
	public void abnormalsetted(AbnormalState state) {
		if ((state.getId() & AbnormalState.CANCEL_ITEM_USE.getId()) != 0)
			tryAbort();
	}

	@Override
	public void boostSkillCost(Skill skill) {
		tryAbort();
	}

	protected final void tryAbort() {
		if (aborted.compareAndSet(false, true)) {
			abort();
			observed.getObserveController().removeObserver(this);
		}
	}

	public abstract void abort();
}
