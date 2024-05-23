package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien
 */
@AIName("rm_1337")
public class RM1337AI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private AtomicBoolean isEventStarted = new AtomicBoolean(false);
	private Future<?> task1, task2;

	public RM1337AI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		PacketSendUtility.broadcastMessage(getOwner(), 1500229, 2000);
	}

	@Override
	public void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	public void handleDied() {
		cancelTask();
		PacketSendUtility.broadcastMessage(getOwner(), 1500231);
		super.handleDied();
	}

	@Override
	public void handleBackHome() {
		cancelTask();
		super.handleBackHome();
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			startSkillTask1();
		}
		if (getLifeStats().getHpPercentage() <= 75) {
			if (isEventStarted.compareAndSet(false, true)) {
				startSkillTask2();
			}
		}
	}

	private void cancelTask() {
		if (task1 != null && !task1.isCancelled()) {
			task1.cancel(true);
		}
		if (task2 != null && !task2.isCancelled()) {
			task2.cancel(true);
		}
	}

	private void startSkillTask1() {
		task1 = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead()) {
					cancelTask();
				} else {
					if (getOwner().getCastingSkill() != null)
						return;
					if (getLifeStats().getHpPercentage() <= 50) {
						switch (Rnd.nextInt(2)) {
							case 0:
								SkillEngine.getInstance().getSkill(getOwner(), 19550, 10, getTargetPlayer()).useNoAnimationSkill();
								break;
							default:
								final Player target = getTargetPlayer();
								SkillEngine.getInstance().getSkill(getOwner(), 19552, 10, target).useNoAnimationSkill();
								ThreadPoolManager.getInstance().schedule(new Runnable() {

									@Override
									public void run() {
										if (!isDead()) {
											SkillEngine.getInstance().getSkill(getOwner(), 19553, 10, target).useNoAnimationSkill();
										}
									}
								}, 4000);
						}
					} else
						SkillEngine.getInstance().getSkill(getOwner(), 19550, 10, getTargetPlayer()).useNoAnimationSkill();
				}
			}
		}, 10000, 23000);
	}

	private void startSkillTask2() {
		task2 = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead()) {
					cancelTask();
				} else {
					getOwner().getController().cancelCurrentSkill(null);
					PacketSendUtility.broadcastMessage(getOwner(), 1500230);
					SkillEngine.getInstance().getSkill(getOwner(), 19551, 10, getTarget()).useNoAnimationSkill();
					spawnSparks();
				}
			}
		}, 0, 60000);
	}

	private void spawnSparks() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isDead()) {
					int count = Rnd.get(8, 12);
					for (int i = 0; i < count; i++) {
						rndSpawnInRange(282373, 3, 12);
					}
				}
			}
		}, 4000);
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
}
