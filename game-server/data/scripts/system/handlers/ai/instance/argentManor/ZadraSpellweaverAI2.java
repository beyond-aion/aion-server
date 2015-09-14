package ai.instance.argentManor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.manager.EmoteManager;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@AIName("zadra_spellweaver")
public class ZadraSpellweaverAI2 extends AggressiveNpcAI2 {

	private boolean canThink = true;
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private AtomicBoolean startedEvent = new AtomicBoolean(false);
	private AtomicBoolean isSpawnedWings = new AtomicBoolean(false);
	private List<Integer> percents = new ArrayList<Integer>();
	private Future<?> phaseTask;
	private Future<?> skillTask;
	private Future<?> thinkTask;
	private int curentPercent = 100;

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		super.handleCreatureMoved(creature);
		if (getNpcId() == 217240 && creature instanceof Player) {
			if (startedEvent.compareAndSet(false, true)) {
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1500479, getObjectId(), 0, 5000);
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1500478, getObjectId(), 0, 8000);
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						if (!isAlreadyDead()) {
							Npc npc = getPosition().getWorldMapInstance().getNpc(282266);
							if (npc != null) {
								getOwner().getController().onAttack(npc, getLifeStats().getMaxHp() + 1, true);
								NpcActions.delete(npc);
							} else {
								AI2Actions.deleteOwner(ZadraSpellweaverAI2.this);
							}
							spawn(217242, 864.899f, 1234.458f, 194.979f, (byte) 30);
						}
					}

				}, 12000);
			}
		}
	}

	private void startPhaseTask() {
		phaseTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead()) {
					cancelPhaseTask();
				} else {
					sendMsg(1500482);
					useToTargetSkill(19717);
					if (getLifeStats().getHpPercentage() <= 90) {
						startSkillTask();
					}
				}
			}

		}, 3000, 45000);
	}

	private void useToTargetSkill(int skillId) {
		VisibleObject obj = getTarget();
		if (obj != null && obj instanceof Player) {
			Player player = (Player) obj;
			if (!PlayerActions.isAlreadyDead(player)) {
				SkillEngine.getInstance().getSkill(getOwner(), skillId, 60, player).useNoAnimationSkill();
			}
		}
	}

	private void startSkillTask() {
		skillTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					sendMsg(1500484);
					useToTargetSkill(19824);
				}
			}

		}, 25000);
	}

	private void startStopThikTask() {
		thinkTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					startRobotEvent();
				}
			}

		}, 23000);
	}

	private void cancelSkillTask() {
		if (skillTask != null && !skillTask.isDone()) {
			skillTask.cancel(true);
		}
	}

	private void cancelPhaseTask() {
		if (phaseTask != null && !phaseTask.isDone()) {
			phaseTask.cancel(true);
		}
	}

	private void cancelThinkTask() {
		if (thinkTask != null && !thinkTask.isDone()) {
			thinkTask.cancel(true);
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			if (getNpcId() == 217241) {
				getPosition().getWorldMapInstance().getDoors().get(10).setOpen(false);
			} else if (getNpcId() == 217242) {
				sendMsg(1500480);
				startPhaseTask();
			}
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getNpcId() == 217241) {
			addPercent();
			resetHelpers();
		} else if (getNpcId() == 217242) {
			NpcShoutsService.getInstance().sendMsg(getOwner(), 1500481, getObjectId(), 0, 4000);
		} else {
			canThink = false;
		}
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (!isAlreadyDead()) {
			if (getNpcId() == 217241) {
				if (!canThink && isInState(AIState.FOLLOWING)) {
					startThink();
					VisibleObject obj = getTarget();
					if (obj != null && obj instanceof Npc) {
						Npc npc = (Npc) obj;
						if (npc.getNpcId() == 282209) {
							getMoveController().abortMove();
							SkillEngine.getInstance().getSkill(npc, 19432, 60, getOwner()).useNoAnimationSkill();
							SkillEngine.getInstance().getSkill(npc, 19709, 60, getOwner()).useNoAnimationSkill();
							NpcShoutsService.getInstance().sendMsg(getOwner(), 1401042);
							int skill = 0;
							switch (curentPercent) {
								case 80:
									skill = 19384;
									break;
								case 70:
									skill = 19386;
									break;
								case 55:
									skill = 19431;
									break;
							}
							SkillEngine.getInstance().getSkill(npc, skill, 55, getOwner()).useNoAnimationSkill();
						} else if (npc.getNpcId() == 730393) {
							AI2Actions.deleteOwner(this);
						}
					}
				}
			} else if (getNpcId() == 217242) {
				if (!canThink && isInState(AIState.WALKING)) {
					if (!isAlreadyDead()) {
						getSpawnTemplate().setWalkerId(null);
						WalkManager.stopWalking(this);
						WorldMapInstance instance = getPosition().getWorldMapInstance();
						if (instance != null) {
							spawn(282269, 864.977f, 1234.479f, 194.79f, (byte) 116);
							deleteNpcs(instance.getNpcs(282462));
							startThink();
							SkillEngine.getInstance().getSkill(getOwner(), 19395, 60, getOwner()).useNoAnimationSkill();
							Npc robot = instance.getNpc(282189);
							if (robot != null && !NpcActions.isAlreadyDead(robot)) {
								Npc chastisement = instance.getNpc(282269);
								if (chastisement != null && !NpcActions.isAlreadyDead(chastisement)) {
									SkillEngine.getInstance().getSkill(robot, 19394, 55, chastisement).useNoAnimationSkill();
									SkillEngine.getInstance().getSkill(chastisement, 19392, 60, chastisement).useNoAnimationSkill();
								}
							}
						}
					}
				}
			}
		}
	}

	private synchronized void checkPercentage(int hpPercentage) {
		if (getNpcId() == 217241) {
			for (Integer percent : percents) {
				if (hpPercentage <= percent) {
					percents.remove(percent);
					canThink = false;
					EmoteManager.emoteStopAttacking(getOwner());
					switch (percent) {
						case 80:
							moveToSurkana((Npc) spawn(282209, 1020.130f, 1269.600f, 95.95f, (byte) 0));
							break;
						case 70:
							moveToSurkana((Npc) spawn(282209, 1006.090f, 1282.350f, 95.95f, (byte) 0));
							break;
						case 55:
							moveToSurkana((Npc) spawn(282209, 992.511f, 1269.610f, 95.95f, (byte) 0));
							break;
						case 40:
							sendMsg(1500477);
							SkillEngine.getInstance().getSkill(getOwner(), 19385, 60, getOwner()).useNoAnimationSkill();
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									if (!isAlreadyDead()) {
										WorldMapInstance instance = getPosition().getWorldMapInstance();
										if (instance != null) {
											Npc door = (Npc) spawn(730393, 1007.365f, 1306.107f, 94.347f, (byte) 90, 519);
											if (door == null) {
												return;
											}
											getOwner().setTarget(door);
											setStateIfNot(AIState.FOLLOWING);
											getOwner().setState(1);
											getOwner().getMoveController().moveToTargetObject();
											PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
											instance.getDoors().get(10).setOpen(true);
											instance.getDoors().get(210).setOpen(true);
										}
									}
								}

							}, 2000);
							break;
					}
					curentPercent = percent;
					break;
				}
			}
		} else if (getNpcId() == 217242) {
			if (hpPercentage <= 75 && hpPercentage > 35) {
				if (isSpawnedWings.compareAndSet(false, true)) {
					sendMsg(1500483);
					spawn(282246, 879.4466f, 1217.424f, 194.9459f, (byte) 0);
					spawn(282246, 878.6958f, 1216.5687f, 194.9459f, (byte) 0);
				}
			}
			if (hpPercentage <= 35) {
				if (startedEvent.compareAndSet(false, true)) {
					startRobotEvent();
					WorldMapInstance instance = getPosition().getWorldMapInstance();
					if (instance != null) {
						deleteNpcs(instance.getNpcs(282246));
						if (instance.getNpc(282189) == null) {
							instance.getDoors().get(517).setOpen(true);
							spawn(282189, 829.4f, 1240f, 185f, (byte) 116);
							spawn(282462, 843.153f, 1243.670f, 210.891f, (byte) 0);
						}
					}
				}
			}
		}
	}

	private void startRobotEvent() {
		canThink = false;
		cancelSkillTask();
		cancelPhaseTask();
		EmoteManager.emoteStopAttacking(getOwner());
		sendMsg(1500485);
		getSpawnTemplate().setWalkerId("3001500003");
		WalkManager.startWalking(this);
		getOwner().setState(1);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
	}

	private void startThink() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getNpcId() == 217241) {
					VisibleObject obj = getTarget();
					if (obj != null && obj instanceof Npc && ((Npc) obj).getNpcId() != 730393) {
						NpcActions.delete((Npc) obj);
					}
				}
				canThink = true;
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
					if (getNpcId() == 217242) {
						Npc chastisement = getPosition().getWorldMapInstance().getNpc(282269);
						if (chastisement != null && !NpcActions.isAlreadyDead(chastisement)) {
							NpcActions.delete(chastisement);
						}
						startPhaseTask();
						startStopThikTask();
					}
				}
			}
		}, 12000);
	}

	private void moveToSurkana(final Npc npc) {
		SpawnTemplate template = npc.getSpawn();
		for (Npc surkan : getPosition().getWorldMapInstance().getNpcs(282208)) {
			if (surkan.getSpawn().getX() == template.getX() && surkan.getSpawn().getY() == template.getY()) {
				NpcActions.delete(surkan);
				break;
			}
		}
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					getOwner().setTarget(npc);
					setStateIfNot(AIState.FOLLOWING);
					getOwner().setState(1);
					PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
					getOwner().getMoveController().moveToTargetObject();
					startThink();
				}
			}

		}, 3000);
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 80, 70, 55, 40 });
	}

	private void resetHelpers() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null) {
			deleteNpcs(instance.getNpcs(282209));
			deleteNpcs(instance.getNpcs(282208));
			spawn(282208, 1006.090f, 1282.350f, 95.95f, (byte) 0);
			spawn(282208, 1020.130f, 1269.600f, 95.95f, (byte) 0);
			spawn(282208, 992.511f, 1269.610f, 95.95f, (byte) 0);
		}
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}

	private void sendMsg(int msg) {
		NpcShoutsService.getInstance().sendMsg(getOwner(), msg, getObjectId(), 0, 0);
	}

	@Override
	protected void handleDied() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null) {
			if (getNpcId() == 217241) {
				spawn(730393, 1007.365f, 1306.107f, 94.347f, (byte) 90, 519);
				instance.getDoors().get(10).setOpen(true);
				instance.getDoors().get(210).setOpen(true);
				percents.clear();
			} else if (getNpcId() == 217242) {
				cancelSkillTask();
				cancelPhaseTask();
				cancelThinkTask();
				instance.getDoors().get(18).setOpen(true);
				instance.getDoors().get(11).setOpen(true);
				spawn(730382, 864.985f, 1211.849f, 198.863f, (byte) 30, 618);
				deleteNpcs(instance.getNpcs(282189));
				deleteNpcs(instance.getNpcs(282246));
				deleteNpcs(instance.getNpcs(282269));
				sendMsg(1500486);
			}
		}
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		percents.clear();
		super.handleDespawned();
	}

	@Override
	protected void handleBackHome() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null) {
			if (getNpcId() == 217241) {
				curentPercent = 100;
				getEffectController().removeEffect(19709);
				getEffectController().removeEffect(19384);
				getEffectController().removeEffect(19386);
				getEffectController().removeEffect(19431);
				resetHelpers();
				addPercent();
				instance.getDoors().get(10).setOpen(true);
				deleteNpcs(instance.getNpcs(282246));
			} else if (getNpcId() == 217242) {
				deleteNpcs(instance.getNpcs(282269));
			}
		}
		cancelSkillTask();
		cancelThinkTask();
		cancelPhaseTask();
		canThink = true;
		isSpawnedWings.set(false);
		isHome.set(true);
		super.handleBackHome();
	}
}
