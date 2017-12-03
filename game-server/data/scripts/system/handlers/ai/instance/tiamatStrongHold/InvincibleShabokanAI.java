package ai.instance.tiamatStrongHold;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("invincibleshabokan")
public class InvincibleShabokanAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> skillTask;
	private boolean isFinalBuff;

	public InvincibleShabokanAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false))
			startSkillTask();
		if (!isFinalBuff && getOwner().getLifeStats().getHpPercentage() <= 25) {
			isFinalBuff = true;
			AIActions.useSkill(this, 20941);
		}
	}

	private void startSkillTask() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead())
					cancelTask();
				else {
					chooseRandomEvent();
				}
			}
		}, 5000, 30000);
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	private void earthQuakeEvent() {
		Npc invisible = getPosition().getWorldMapInstance().getNpc(283082);// 4.0
		SkillEngine.getInstance().getSkill(getOwner(), 20717, 55, getOwner()).useNoAnimationSkill();
		if (invisible == null) {
			spawn(283082, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);// 4.0
		}
	}

	private void sinkEvent() {
		SkillEngine.getInstance().getSkill(getOwner(), 20720, 55, getOwner()).useNoAnimationSkill();
		for (Player player : getKnownList().getKnownPlayers().values()) {
			if (isInRange(player, 30)) {
				spawn(283083, player.getX(), player.getY(), player.getZ(), (byte) 0);// 4.0
				spawn(283084, player.getX(), player.getY(), player.getZ(), (byte) 0);// 4.0
			}
		}
	}

	private void chooseRandomEvent() {
		int rand = Rnd.get(0, 1);
		if (rand == 0)
			earthQuakeEvent();
		else
			sinkEvent();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelTask();
		getOwner().getEffectController().removeEffect(20941);
		isHome.set(true);
	}
}
