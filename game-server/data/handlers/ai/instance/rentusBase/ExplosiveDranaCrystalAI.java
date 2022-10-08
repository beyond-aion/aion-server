package ai.instance.rentusBase;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.ActionItemNpcAI;

/**
 * @author xTz
 */
@AIName("explosive_drana_crystal")
public class ExplosiveDranaCrystalAI extends ActionItemNpcAI {

	private AtomicBoolean isUsed = new AtomicBoolean(false);
	private Future<?> lifeTask;

	public ExplosiveDranaCrystalAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startLifeTask();
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (isUsed.compareAndSet(false, true)) {
			WorldPosition p = getPosition();
			Npc boss = p.getWorldMapInstance().getNpc(217308);
			if (boss != null && !boss.isDead()) {
				EffectController ef = boss.getEffectController();
				if (ef.hasAbnormalEffect(19370)) {
					ef.removeEffect(19370);
				} else if (ef.hasAbnormalEffect(19371)) {
					ef.removeEffect(19371);
				} else if (ef.hasAbnormalEffect(19372)) {
					ef.removeEffect(19372);
				}
			}
			Npc npc = (Npc) spawn(282530, p.getX(), p.getY(), p.getZ(), p.getHeading());
			Npc invisibleNpc = (Npc) spawn(282529, p.getX(), p.getY(), p.getZ(), p.getHeading());
			SkillEngine.getInstance().getSkill(npc, 19373, 60, npc).useNoAnimationSkill();
			SkillEngine.getInstance().getSkill(invisibleNpc, 19654, 60, invisibleNpc).useNoAnimationSkill();
			invisibleNpc.getController().delete();
			AIActions.deleteOwner(this);
		}
	}

	private void cancelLifeTask() {
		if (lifeTask != null && !lifeTask.isDone()) {
			lifeTask.cancel(true);
		}
	}

	private void startLifeTask() {
		lifeTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				AIActions.deleteOwner(ExplosiveDranaCrystalAI.this);
		}, 60000);
	}

	@Override
	protected void handleDied() {
		cancelLifeTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelLifeTask();
		super.handleDespawned();
	}

}
