package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
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
public class MagePreceptorAI extends AggressiveNpcAI {

	private List<Integer> percents = new ArrayList<>();

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		addPercents();
	}

	@Override
	public void handleDespawned() {
		percents.clear();
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
		addPercents();
		despawnNpcs();
		super.handleBackHome();
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void startEvent(int percent) {
		if (percent == 50 || percent == 25) {
			SkillEngine.getInstance().getSkill(getOwner(), 19606, 10, getTarget()).useNoAnimationSkill();
		}

		switch (percent) {
			case 75:
				SkillEngine.getInstance().getSkill(getOwner(), 19605, 10, getTargetPlayer()).useNoAnimationSkill();
				break;
			case 50:
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						if (!isDead()) {
							SkillEngine.getInstance().getSkill(getOwner(), 19609, 10, getOwner()).useNoAnimationSkill();
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									WorldPosition p = getPosition();
									spawn(282364, p.getX(), p.getY(), p.getZ(), p.getHeading());
									spawn(282363, p.getX(), p.getY(), p.getZ(), p.getHeading());
									scheduleSkill(2000);
								}

							}, 4500);

						}
					}
				}, 3000);
				break;
			case 25:
				scheduleSkill(3000);
				scheduleSkill(9000);
				scheduleSkill(15000);
				break;
		}

	}

	private void scheduleSkill(int delay) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 19605, 10, getTargetPlayer()).useNoAnimationSkill();

				}
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

	private void checkPercentage(int percentage) {
		for (Integer percent : percents) {
			if (percentage <= percent) {
				percents.remove(percent);
				startEvent(percent);
				break;
			}
		}
	}

	private void addPercents() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 75, 50, 25 });
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
