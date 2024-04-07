package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;
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
				SkillEngine.getInstance().getSkill(getOwner(), 19605, 10, getTargetPlayer()).useNoAnimationSkill();
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
				SkillEngine.getInstance().getSkill(getOwner(), 19605, 10, getTargetPlayer()).useNoAnimationSkill();
			}
		}, delay);
	}

	private Player getTargetPlayer() {
		List<Player> players = new ArrayList<>();
		getKnownList().forEachPlayer(player -> {
			if (!player.isDead() && PositionUtil.isInRange(player, getOwner(), 37)) {
				players.add(player);
			}
		});
		return Rnd.get(players);
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
