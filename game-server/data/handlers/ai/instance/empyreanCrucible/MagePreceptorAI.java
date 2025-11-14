package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.controllers.attack.AggroTarget;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Luzien
 */
@AIName("mage_preceptor")
public class MagePreceptorAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(75, 50, 25);

	public MagePreceptorAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleDespawned() {
		despawnNpcs();
		super.handleDespawned();
	}

	@Override
	public void handleDied() {
		despawnNpcs();
		super.handleDied();
	}

	@Override
	public void handleBackHome() {
		despawnNpcs();
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 75:
				SkillEngine.getInstance().getSkill(getOwner(), 19605, 10, getRandomTarget()).useNoAnimationSkill();
				break;
			case 50:
				SkillEngine.getInstance().getSkill(getOwner(), 19606, 10, getTarget()).useNoAnimationSkill();
				ThreadPoolManager.getInstance().schedule(() -> {
					if (!isDead()) {
						SkillEngine.getInstance().getSkill(getOwner(), 19609, 10, getOwner()).useNoAnimationSkill();
						ThreadPoolManager.getInstance().schedule(() -> {
							WorldPosition p = getPosition();
							spawn(282364, p.getX(), p.getY(), p.getZ(), p.getHeading());
							spawn(282363, p.getX(), p.getY(), p.getZ(), p.getHeading());
							scheduleSkill(2000);
						}, 4500);
					}
				}, 3000);
				break;
			case 25:
				SkillEngine.getInstance().getSkill(getOwner(), 19606, 10, getTarget()).useNoAnimationSkill();
				scheduleSkill(3000);
				scheduleSkill(9000);
				scheduleSkill(15000);
				break;
		}
	}

	private void scheduleSkill(int delay) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				SkillEngine.getInstance().getSkill(getOwner(), 19605, 10, getRandomTarget()).useNoAnimationSkill();
			}
		}, delay);
	}

	private Creature getRandomTarget() {
		return getAggroList().getTarget(AggroTarget.RANDOM, 37);
	}

	private void despawnNpcs() {
		despawnNpc(getPosition().getWorldMapInstance().getNpc(282364));
		despawnNpc(getPosition().getWorldMapInstance().getNpc(282363));
	}

	private void despawnNpc(Npc npc) {
		if (npc != null) {
			npc.getController().delete();
		}
	}
}
