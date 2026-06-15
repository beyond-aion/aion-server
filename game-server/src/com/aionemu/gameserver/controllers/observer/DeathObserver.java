package com.aionemu.gameserver.controllers.observer;

import java.util.function.Consumer;

import com.aionemu.gameserver.model.gameobjects.Creature;

public class DeathObserver extends ActionObserver {

	private final Consumer<Creature> actionOnDeath;

	public DeathObserver(Consumer<Creature> actionOnDeath) {
		super(ObserverType.DEATH);
		this.actionOnDeath = actionOnDeath;
	}

	@Override
	public void died(Creature lastAttacker) {
		actionOnDeath.accept(lastAttacker);
	}
}
