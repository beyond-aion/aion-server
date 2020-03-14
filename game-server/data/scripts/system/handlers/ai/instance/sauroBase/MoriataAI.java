package ai.instance.sauroBase;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl March 22nd, 2018
 */
@AIName("moriata")
public class MoriataAI extends AggressiveNpcAI {

	private AtomicBoolean hasAggro = new AtomicBoolean();
	private Future<?> task;
	private long lastSkillUse;
	private int kiteCount;

	public MoriataAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (hasAggro.compareAndSet(false, true))
			task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> observeKiting(), 12000, 12000);
	}

	private void observeKiting() {
		if ((System.currentTimeMillis() - lastSkillUse) / 1000 >= 10) {
			SkillEngine.getInstance().applyEffectDirectly(20181, getOwner(), getOwner()); // Temporary Speed Buff
			SkillEngine.getInstance().applyEffectDirectly(21417, getOwner(), getOwner()); // Raging Fury
			if (++kiteCount % 3 == 0)
				SkillEngine.getInstance().applyEffectDirectly(22661, getOwner(), getOwner()); // Permanent Speed Buff
		}
	}

	@Override
	protected void handleAttackComplete() {
		lastSkillUse = System.currentTimeMillis();
		super.handleAttackComplete();
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		lastSkillUse = System.currentTimeMillis();
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}

	@Override
	protected void handleBackHome() {
		cancelTask();
		hasAggro.set(false);
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		cancelTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}
}
