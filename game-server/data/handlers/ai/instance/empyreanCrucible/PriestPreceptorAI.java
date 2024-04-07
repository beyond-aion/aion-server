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
@AIName("priest_preceptor")
public class PriestPreceptorAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(75, 25);

	public PriestPreceptorAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> SkillEngine.getInstance().getSkill(getOwner(), 19612, 15, getOwner()).useNoAnimationSkill(), 1000);
	}

	@Override
	public void handleBackHome() {
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
			case 75 -> SkillEngine.getInstance().getSkill(getOwner(), 19611, 10, getTargetPlayer()).useNoAnimationSkill();
			case 25 -> startEvent();
		}
	}

	private void startEvent() {
		SkillEngine.getInstance().getSkill(getOwner(), 19610, 10, getOwner()).useNoAnimationSkill();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				SkillEngine.getInstance().getSkill(getOwner(), 19614, 10, getOwner()).useNoAnimationSkill();

				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						WorldPosition p = getPosition();
						applySoulSickness((Npc) spawn(282366, p.getX(), p.getY(), p.getZ(), p.getHeading()));
						applySoulSickness((Npc) spawn(282367, p.getX(), p.getY(), p.getZ(), p.getHeading()));
						applySoulSickness((Npc) spawn(282368, p.getX(), p.getY(), p.getZ(), p.getHeading()));
					}
				}, 5000);
			}

		}, 2000);
	}

	private Player getTargetPlayer() {
		List<Player> players = new ArrayList<>();
		getKnownList().forEachPlayer(player -> {
			if (!player.isDead() && PositionUtil.isInRange(player, getOwner(), 25)) {
				players.add(player);
			}
		});
		return Rnd.get(players);
	}

	private void applySoulSickness(final Npc npc) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				npc.getLifeStats().setCurrentHpPercent(50); // TODO: remove this, fix max hp debuffs not reducing current hp properly
				SkillEngine.getInstance().getSkill(npc, 19594, 4, npc).useNoAnimationSkill();
			}

		}, 1000);
	}

}
