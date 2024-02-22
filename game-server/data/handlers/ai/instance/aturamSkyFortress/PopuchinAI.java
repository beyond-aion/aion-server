package ai.instance.aturamSkyFortress;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("popuchin")
public class PopuchinAI extends AggressiveNpcAI {

	private boolean isHome = true;
	private Future<?> bombTask;

	public PopuchinAI(Npc owner) {
		super(owner);
	}

	private void startBombTask() {
		if (!isDead() && !isHome) {
			bombTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (!isDead() && !isHome) {
						VisibleObject target = getTarget();
						if (target instanceof Player) {
							SkillEngine.getInstance().getSkill(getOwner(), 19413, 49, target).useNoAnimationSkill();
						}
						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								if (!isDead() && !isHome) {
									SkillEngine.getInstance().getSkill(getOwner(), 19412, 49, getOwner()).useNoAnimationSkill();
									ThreadPoolManager.getInstance().schedule(new Runnable() {

										@Override
										public void run() {
											if (!isDead() && !isHome && getOwner().isSpawned()) {
												if (getLifeStats().getHpPercentage() > 50) {
													WorldPosition p = getPosition();
													if (p != null && p.getWorldMapInstance() != null) {
														spawn(217374, p.getX(), p.getY(), p.getZ(), p.getHeading());
														spawn(217374, p.getX(), p.getY(), p.getZ(), p.getHeading());
														startBombTask();
													}
												} else {
													spawnRndBombs();
													startBombTask();
												}
											}
										}

									}, 1500);
								}
							}

						}, 3000);
					}
				}

			}, 15500);
		}
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome) {
			isHome = false;
			getPosition().getWorldMapInstance().setDoorState(68, false);
			startBombTask();
		}
	}

	@Override
	protected void handleBackHome() {
		isHome = true;
		super.handleBackHome();
		getPosition().getWorldMapInstance().setDoorState(68, true);
		if (bombTask != null && !bombTask.isDone()) {
			bombTask.cancel(true);
		}
	}

	private void spawnRndBombs() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isDead() && !isHome) {
					for (int i = 0; i < 10; i++) {
						rndSpawnInRange(217375, 1, 12);
					}
				}
			}

		}, 1500);

	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		getPosition().getWorldMapInstance().setDoorState(68, true);
	}

}
