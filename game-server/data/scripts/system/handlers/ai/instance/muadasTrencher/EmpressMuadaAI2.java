package ai.instance.muadasTrencher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.manager.EmoteManager;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI2;
import javolution.util.FastTable;

/**
 * @author xTz
 * @reworked Whoop
 */
@AIName("empress_muada")
public class EmpressMuadaAI2 extends AggressiveNpcAI2 {

	private boolean think = true;
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private AtomicBoolean neuroToxin = new AtomicBoolean(false);
	private Future<?> enrageTask;
	private Future<?> rayThornTask;
	private Future<?> addSpawnTask;
	private Future<?> swipingBlowTask;
	private Future<?> neuroToxinTask;
	protected List<Integer> percents = new FastTable<>();

	@Override
	public boolean canThink() {
		return think;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			startEnrageTask();
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				switch (percent) {
					case 100:
						startRayThornEvent();
						break;
					case 75:
						AI2Actions.useSkill(this, 19898);
						cancelRayThorn();
						startAddSpawnEvent();
						break;
					case 50:
						AI2Actions.useSkill(this, 19898);
						cancelAddSpawn();
						deleteAdds();
						startSwipingBlowEvent();
						break;
					case 25:
						AI2Actions.useSkill(this, 19898);
						cancelSwipingBlow();
						sandSquallNeuroToxinEvent();
						break;
				}
				percents.remove(percent);
				break;
			}
		}
	}

	private void sandSquallNeuroToxinEvent() {
		think = false;
		EmoteManager.emoteStopAttacking(getOwner());
		SkillEngine.getInstance().getSkill(getOwner(), 20499, 2, getOwner()).useNoAnimationSkill();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 19893, 2, getOwner()).useNoAnimationSkill();
					spawn(282533, 523.1f, 541.1f, 106.7f, (byte) 0);
					PacketSendUtility.broadcastToMap(getOwner(), 1401300);
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (!isAlreadyDead()) {
								think = true;
								Creature creature = getAggroList().getMostHated();
								if (creature == null || creature.getLifeStats().isAlreadyDead() || !getOwner().canSee(creature)) {
									setStateIfNot(AIState.FIGHT);
									think();
								} else {
									getMoveController().abortMove();
									getOwner().setTarget(creature);
									getOwner().getGameStats().renewLastAttackTime();
									getOwner().getGameStats().renewLastAttackedTime();
									getOwner().getGameStats().renewLastChangeTargetTime();
									getOwner().getGameStats().renewLastSkillTime();
									setStateIfNot(AIState.FIGHT);
									handleMoveValidate();
									SkillEngine.getInstance().getSkill(getOwner(), 19897, 2, getOwner()).useNoAnimationSkill();
								}
								if (isInState(AIState.FIGHT) && neuroToxin.compareAndSet(false, true)) {
									startNeuroToxinEvent();
								}
							}
						}
					}, 20000);
				}
			}
		}, 3500);
	}

	private void startEnrageTask() {
		enrageTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 19859, 2, getOwner()).useNoAnimationSkill();
					enrageTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (!isAlreadyDead()) {
								SkillEngine.getInstance().getSkill(getOwner(), 20089, 2, getOwner()).useNoAnimationSkill();
							}
						}
					}, 15000);
				}
			}
		}, 1200000); // 20 minutes
	}

	private void startRayThornEvent() {
		rayThornTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelRayThorn();
				else {
					projectileVomit();
					if (!isAlreadyDead()) {
						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								SkillEngine.getInstance().getSkill(getOwner(), 19891, 2, getOwner()).useSkill();
							}
						}, 8000);
					}
				}
			}
		}, 1000, 25000);
	}

	private void startAddSpawnEvent() {
		addSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead()) {
					cancelAddSpawn();
				} else {
					for (int i = 0; i < 3; i++) {
						PacketSendUtility.broadcastToMap(getOwner(), 1401299);
						Point3D p = getRndPos();
						spawn(282556, p.getX(), p.getY(), p.getZ(), (byte) 0);
						Npc npc1 = (Npc) spawn(282535, p.getX(), p.getY(), p.getZ(), (byte) 0);
						Npc npc2 = (Npc) spawn(282535, p.getX(), p.getY(), p.getZ(), (byte) 0);
						PacketSendUtility.broadcastMessage(npc1, 1500307, 1000);
						PacketSendUtility.broadcastMessage(npc2, 1500307, 1000);
					}
				}
			}
		}, 6000, 45000);
	}

	private void startSwipingBlowEvent() {
		swipingBlowTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelSwipingBlow();
				else {
					swipingBlow();
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (!isAlreadyDead())
								SkillEngine.getInstance().getSkill(getOwner(), 19891, 2, getOwner()).useSkill();
						}
					}, 6000);
				}
			}
		}, 8000, 30000);
	}

	private void startNeuroToxinEvent() {
		neuroToxinTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelNeuroToxin();
				else {
					neurotoxin();
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (isAlreadyDead())
								cancelNeuroToxin();
							else {
								neurotoxin();
								ThreadPoolManager.getInstance().schedule(new Runnable() {

									@Override
									public void run() {
										if (isAlreadyDead())
											cancelNeuroToxin();
										else {
											projectileVomit();
											ThreadPoolManager.getInstance().schedule(new Runnable() {

												@Override
												public void run() {
													if (isAlreadyDead())
														cancelNeuroToxin();
													else {
														swipingBlow();
													}
												}
											}, 5000);
										}
									}
								}, 12000);
							}
						}
					}, 3000);
				}
			}
		}, 8000, 30000);
	}

	private void projectileVomit() {
		if (!isAlreadyDead())
			SkillEngine.getInstance().getSkill(getOwner(), 19890, 2, getOwner()).useSkill();
	}

	private void swipingBlow() {
		if (!isAlreadyDead()) {
			AI2Actions.targetSelf(this);
			SkillEngine.getInstance().getSkill(getOwner(), 19889, 2, getOwner()).useSkill();
		}
	}

	private void neurotoxin() {
		Player target = getRandomTarget();
		if (!isAlreadyDead()) {
			AI2Actions.targetCreature(this, target);
			if (target == null)
				return;
			else
				SkillEngine.getInstance().getSkill(getOwner(), 19892, 2, getOwner()).useSkill();
		}
	}

	private Player getRandomTarget() {
		List<Player> players = new FastTable<Player>();
		for (Player player : getKnownList().getKnownPlayers().values()) {
			if (!PlayerActions.isAlreadyDead(player) && MathUtil.isIn3dRange(player, getOwner(), 30)) {
				players.add(player);
			}
		}
		if (players.isEmpty())
			return null;
		return players.get(Rnd.get(players.size()));
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}

	private void deleteAdds() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				WorldPosition p = getPosition();
				if (p != null) {
					WorldMapInstance instance = p.getWorldMapInstance();
					if (instance != null) {
						deleteNpcs(instance.getNpcs(282556));
						deleteNpcs(instance.getNpcs(282535));
						deleteNpcs(instance.getNpcs(282539));
					}
				}
			}
		}, 30000);
	}

	private Point3D getRndPos() {
		float direction = Rnd.get(0, 199) / 100f;
		int distance = Rnd.get(2, 5);
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		return new Point3D(p.getX() + x1, p.getY() + y1, p.getZ());
	}

	private void cancelRayThorn() {
		if (rayThornTask != null && !rayThornTask.isCancelled()) {
			rayThornTask.cancel(true);
		}
	}

	private void cancelAddSpawn() {
		if (addSpawnTask != null && !addSpawnTask.isCancelled()) {
			addSpawnTask.cancel(true);
		}
	}

	private void cancelSwipingBlow() {
		if (swipingBlowTask != null && !swipingBlowTask.isCancelled()) {
			swipingBlowTask.cancel(true);
		}
	}

	private void cancelNeuroToxin() {
		if (neuroToxinTask != null && !neuroToxinTask.isCancelled()) {
			neuroToxinTask.cancel(true);
		}
	}

	private void cancelEnrageTask() {
		if (enrageTask != null && !enrageTask.isCancelled()) {
			enrageTask.cancel(true);
		}
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 100, 75, 50, 25 });
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		isHome.set(true);
		super.handleBackHome();
		deleteAdds();
		cancelEnrageTask();
		cancelRayThorn();
		cancelAddSpawn();
		cancelSwipingBlow();
		cancelNeuroToxin();
		getEffectController().removeEffect(19859);
		think = true;
		neuroToxin.set(false);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		deleteAdds();
		cancelEnrageTask();
		cancelRayThorn();
		cancelAddSpawn();
		cancelSwipingBlow();
		cancelNeuroToxin();

	}

	@Override
	protected void handleDied() {
		percents.clear();
		super.handleDied();
		deleteAdds();
		cancelEnrageTask();
		cancelRayThorn();
		cancelAddSpawn();
		cancelSwipingBlow();
		cancelNeuroToxin();

	}

}
