package ai.instance.theobomosLab;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("triroan_summon")
public class TriroansSummonAI extends AggressiveNpcAI {

	private AtomicBoolean isDestroyed = new AtomicBoolean(false);
	private int walkPosition;
	private int helperSkill;

	public TriroansSummonAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		switch (getNpcId()) {
			case 280975:
				walkPosition = 3;
				helperSkill = 18493;
				break;
			case 280976:
				walkPosition = 4;
				helperSkill = 18492;
				break;
			case 280977:
				walkPosition = 2;
				helperSkill = 18485;
				break;
			case 280978:
				walkPosition = 5;
				helperSkill = 18491;
				break;
		}
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		int point = getOwner().getMoveController().getCurrentStep().getStepIndex();
		if (walkPosition == point) {
			if (isDestroyed.compareAndSet(false, true)) {
				getSpawnTemplate().setWalkerId(null);
				WalkManager.stopWalking(this);
				useSkill();
				startDespawnTask();
			}
		}
	}

	private synchronized void useSkill() {
		Npc boss = getPosition().getWorldMapInstance().getNpc(214669);
		if (boss != null && checkLocation(getOwner()) && !boss.isDead()) {
			SkillEngine.getInstance().getSkill(boss, helperSkill, 50, boss).useSkill();
		} else
			checkSkillUse(boss);
	}

	private void checkSkillUse(final Npc boss) {
		if (boss != null && checkDistance() == 0 && !boss.isDead()) {
			if (!boss.isCasting())
				SkillEngine.getInstance().getSkill(boss, helperSkill, 50, boss).useSkill();
			else {
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						if (checkDistance() == 0 && !boss.isDead())
							checkSkillUse(boss);
					}
				}, 5000);
			}
		}
	}

	private boolean checkLocation(Npc npc) {
		if (checkDistance() == 1 && npc.getNpcId() == 280975)
			return true;
		else if (checkDistance() == 2 && npc.getNpcId() == 280976)
			return true;
		else if (checkDistance() == 3 && npc.getNpcId() == 280977)
			return true;
		else if (checkDistance() == 4 && npc.getNpcId() == 280978)
			return true;
		else
			return false;
	}

	public int checkDistance() {
		Npc boss = getPosition().getWorldMapInstance().getNpc(214669);
		if (PositionUtil.getDistance(boss, 624.002f, 474.241f, 196.160f) <= 5)
			return 1;
		else if (PositionUtil.getDistance(boss, 623.23f, 502.715f, 196.087f) <= 5)
			return 2;
		else if (PositionUtil.getDistance(boss, 579.943f, 500.999f, 196.604f) <= 5)
			return 3;
		else if (PositionUtil.getDistance(boss, 578.323f, 475.784f, 196.463f) <= 5)
			return 4;
		else
			return 0;
	}

	private void startDespawnTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.deleteOwner(TriroansSummonAI.this);
			}
		}, 3000);
	}
}
