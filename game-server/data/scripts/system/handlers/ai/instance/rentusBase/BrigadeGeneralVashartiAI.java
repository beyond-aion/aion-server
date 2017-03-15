package ai.instance.rentusBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("brigade_general_vasharti")
public class BrigadeGeneralVashartiAI extends AggressiveNpcAI {

	private List<Integer> percents = new ArrayList<>();
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private boolean canThink = true;
	private Future<?> flameBuffTask;
	private Future<?> flameSmashTask;
	private List<Point3D> blueFlameSmashs = new ArrayList<>();
	private List<Point3D> redFlameSmashs = new ArrayList<>();
	private int flameSmashCount = 1;
	private AtomicBoolean isInFlameShowerTask = new AtomicBoolean();

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			getPosition().getWorldMapInstance().getDoors().get(70).setOpen(false);
			startFlameBuffEvent();
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (isInFlameShowerTask.get())
			return;
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				cancelFlameBuffEvent();
				getOwner().getQueuedSkills().clear();
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(20532, 1, 100, 0, 10000, true)));
				break;
			}
		}
	}

	@Override
	public void handleMoveArrived() {
		if (isInState(AIState.FORCED_WALKING) && isInFlameShowerTask.get()
			&& PositionUtil.getDistance(getOwner().getX(), getOwner().getY(), 188.17f, 414.06f) <= 1f) {
			getOwner().getMoveController().abortMove();
			setStateIfNot(AIState.FIGHT);
			setSubStateIfNot(AISubState.NONE);
			Creature creature = getAggroList().getMostHated();
			if (creature != null && !creature.getLifeStats().isAlreadyDead() && getOwner().canSee(creature)) {
				getOwner().getQueuedSkills().clear();
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(20533, 1, 100, true)));
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(20534, 1, 100, 0, 10000, true)));
				getOwner().setTarget(creature);
				getOwner().getGameStats().renewLastAttackTime();
				getOwner().getGameStats().renewLastAttackedTime();
				getOwner().getGameStats().renewLastChangeTargetTime();
				getOwner().getGameStats().renewLastSkillTime();
				getOwner().getGameStats().setNextSkillTime(7000);
			}
			think();
		}
		super.handleMoveArrived();
	}

	@Override
	public void onStartUseSkill(NpcSkillEntry startingSkill) {
		switch (startingSkill.getSkillId()) {
			case 20534:
				startAirEvent();
				break;
		}
	}

	@Override
	public void onEndUseSkill(NpcSkillEntry usedSkill) {
		switch (usedSkill.getSkillId()) {
			case 20530:
			case 20531:
				WorldMapInstance instance = getPosition().getWorldMapInstance();
				if (instance != null) {
					if (instance.getNpc(283000) == null && instance.getNpc(283001) == null) {
						VisibleObject ice = spawn(283001, 205.280f, 410.53f, 261f, (byte) 56);
						VisibleObject fire = spawn(283000, 171.330f, 417.57f, 261f, (byte) 116);
						if (ice != null) {
							useKissBuff((Npc) ice);
						}
						if (fire != null) {
							useKissBuff((Npc) fire);
						}
					}
				}
				break;
			case 20532:
				if (isInFlameShowerTask.compareAndSet(false, true)) {
					EmoteManager.emoteStopAttacking(getOwner());
					getOwner().getQueuedSkills().clear();
					ThreadPoolManager.getInstance().schedule(() -> {
						setStateIfNot(AIState.FIGHT);
						setSubStateIfNot(AISubState.NONE);
						WalkManager.startForcedWalking(this, 188.17f, 414.06f, 260.75488f);
						getOwner().setState(CreatureState.ACTIVE, true);
						PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
					}, 1200);
				}
				break;
		}
	}

	private void startAirEvent() {
		int percent = getLifeStats().getHpPercentage();
		int npcId = 0;
		if (percent <= 25) {
			npcId = 283012;
		} else if (percent <= 40) {
			npcId = 283012;
		} else if (percent <= 50) {
			npcId = 283011;
		} else if (percent <= 70) {
			npcId = 283011;
		} else if (percent <= 80) {
			npcId = 283010;
		}

		spawn(npcId, 188.33f, 414.61f, 260.61f, (byte) 244);
		final Npc buffNpc = (Npc) spawn(283007, 188.33f, 414.61f, 260.61f, (byte) 0);

		ThreadPoolManager.getInstance().schedule(() -> {
			if (!buffNpc.getLifeStats().isAlreadyDead()) {
				startFlameSmashEvent(percent);
				SkillEngine.getInstance().getSkill(buffNpc, 20538, 60, buffNpc).useNoAnimationSkill();
				ThreadPoolManager.getInstance().schedule(() -> buffNpc.getController().delete(), 4000);
			}
		}, 1000);

		ThreadPoolManager.getInstance().schedule(() -> {
			cancelFlameSmashTask();
			cancelAirEvent();
			startFlameBuffEvent();
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
			}
		}, 40000);
	}

	private void cancelFlameSmashTask() {
		flameSmashCount = 1;
		if (flameSmashTask != null && !flameSmashTask.isDone()) {
			flameSmashTask.cancel(true);
		}
	}

	private void startFlameSmashEvent(final int percent) {
		flameSmashTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isAlreadyDead()) {
				cancelFlameSmashTask();
			} else {
				List<Point3D> redFlameSmashs1 = getRedFlameSmashs(283008);
				List<Point3D> blueFlameSmashs1 = getRedFlameSmashs(283009);
				WorldMapInstance instance = getPosition().getWorldMapInstance();
				if (instance != null) {
					if (percent > 40 && flameSmashCount == 1) {
						flameSmashCount++;
						spawnFlameSmash(redFlameSmashs1, 283008);
						spawnFlameSmash(blueFlameSmashs1, 283009);
					} else {
						if (instance.getNpc(283010) != null) {
							flameSmashCount = 1;
							spawnFlameSmash(redFlameSmashs1, 283008);
							spawnFlameSmash(redFlameSmashs1, 283008);
							spawnFlameSmash(redFlameSmashs1, 283008);
						} else if (instance.getNpc(283011) != null) {
							flameSmashCount = 1;
							spawnFlameSmash(blueFlameSmashs1, 283009);
							spawnFlameSmash(blueFlameSmashs1, 283009);
							spawnFlameSmash(blueFlameSmashs1, 283009);
						} else if (instance.getNpc(283012) != null) {
							if (flameSmashCount == 1) {
								flameSmashCount++;
								spawnFlameSmash(redFlameSmashs1, 283008);
								spawnFlameSmash(redFlameSmashs1, 283008);
								spawnFlameSmash(blueFlameSmashs1, 283009);
								spawnFlameSmash(blueFlameSmashs1, 283009);
							} else if (flameSmashCount == 2) {
								flameSmashCount++;
								spawnFlameSmash(redFlameSmashs1, 283008);
								spawnFlameSmash(redFlameSmashs1, 283008);
								spawnFlameSmash(redFlameSmashs1, 283008);
							} else {
								flameSmashCount = 1;
								spawnFlameSmash(blueFlameSmashs1, 283009);
								spawnFlameSmash(blueFlameSmashs1, 283009);
								spawnFlameSmash(blueFlameSmashs1, 283009);
							}
						}
					}
				}
				redFlameSmashs1.clear();
				blueFlameSmashs1.clear();
			}
		}, 3000, 3000);
	}

	private void spawnFlameSmash(List<Point3D> flameSmashs, int npcId) {
		if (!flameSmashs.isEmpty()) {
			Point3D spawn = flameSmashs.remove(Rnd.get(flameSmashs.size()));
			spawn(npcId, spawn.getX(), spawn.getY(), spawn.getZ(), (byte) 0);
		}
	}

	private boolean isSpawned(int npcId, Point3D position) {
		for (Npc npc : getPosition().getWorldMapInstance().getNpcs(npcId)) {
			if (npc.getX() == position.getX() && npc.getY() == position.getY()) {
				return true;
			}
		}
		return false;
	}

	private List<Point3D> getRedFlameSmashs(int npcId) {
		return (npcId == 283008 ? redFlameSmashs : blueFlameSmashs).stream().filter(flameSmash -> !isSpawned(npcId, flameSmash))
			.collect(Collectors.toList());
	}

	private void deleteNpcs(List<Npc> npcs) {
		npcs.stream().filter(npc -> npc != null).forEach(npc -> npc.getController().delete());
	}

	private void cancelAirEvent() {
		isInFlameShowerTask.set(false);
		if (!isAlreadyDead()) {
			getOwner().getEffectController().removeEffect(20534);
		}
		if (getPosition() != null) {
			WorldMapInstance instance = getPosition().getWorldMapInstance();
			if (instance != null) {
				deleteNpcs(instance.getNpcs(283002));
				deleteNpcs(instance.getNpcs(283003));
				deleteNpcs(instance.getNpcs(283004));
				deleteNpcs(instance.getNpcs(283005));
				deleteNpcs(instance.getNpcs(283006));
				deleteNpcs(instance.getNpcs(283007));
				deleteNpcs(instance.getNpcs(283010));
				deleteNpcs(instance.getNpcs(283011));
				deleteNpcs(instance.getNpcs(283012));
				deleteNpcs(instance.getNpcs(283000));
				deleteNpcs(instance.getNpcs(283001));
			}
		}
	}

	private void cancelFlameBuffEvent() {
		if (flameBuffTask != null && !flameBuffTask.isDone()) {
			flameBuffTask.cancel(true);
		}
		getOwner().getQueuedSkills().clear();
	}

	private void startFlameBuffEvent() {
		flameBuffTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isAlreadyDead() || !getOwner().isSpawned()) {
				cancelFlameBuffEvent();
			} else {
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(Rnd.get(0, 1) == 0 ? 20530 : 20531, 60, 100, true)));
			}
		}, 4000, 40000);
	}

	private void useKissBuff(Npc npc) {
		npc.getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate((npc.getNpcId() == 283001 ? 19346 : 19345), 60, 100, true)));
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, 80, 70, 50, 40, 25);
	}

	@Override
	protected void handleSpawned() {
		addPercent();
		super.handleSpawned();
		PacketSendUtility.broadcastMessage(getOwner(), 1500405, 2000);
		blueFlameSmashs.add(new Point3D(176.184f, 415.782f, 260.572f));
		blueFlameSmashs.add(new Point3D(159.480f, 412.495f, 260.555f));
		blueFlameSmashs.add(new Point3D(183.784f, 413.475f, 260.755f));
		blueFlameSmashs.add(new Point3D(211.497f, 398.355f, 260.550f));
		blueFlameSmashs.add(new Point3D(173.654f, 419.605f, 260.571f));
		blueFlameSmashs.add(new Point3D(168.923f, 397.403f, 260.571f));
		blueFlameSmashs.add(new Point3D(189.839f, 385.524f, 260.571f));
		blueFlameSmashs.add(new Point3D(209.488f, 398.908f, 260.552f));
		blueFlameSmashs.add(new Point3D(171.938f, 419.449f, 260.571f));
		blueFlameSmashs.add(new Point3D(202.292f, 403.402f, 260.559f));
		blueFlameSmashs.add(new Point3D(184.120f, 384.201f, 260.571f));
		blueFlameSmashs.add(new Point3D(178.429f, 415.372f, 260.572f));
		redFlameSmashs.add(new Point3D(177.106f, 413.678f, 260.569f));
		redFlameSmashs.add(new Point3D(167.271f, 418.102f, 260.721f));
		redFlameSmashs.add(new Point3D(188.180f, 406.594f, 260.572f));
		redFlameSmashs.add(new Point3D(181.117f, 402.296f, 260.571f));
		redFlameSmashs.add(new Point3D(176.960f, 411.328f, 260.550f));
		redFlameSmashs.add(new Point3D(196.692f, 408.827f, 260.564f));
		redFlameSmashs.add(new Point3D(204.483f, 390.333f, 260.565f));
		redFlameSmashs.add(new Point3D(205.820f, 412.985f, 260.571f));
		redFlameSmashs.add(new Point3D(167.999f, 416.711f, 260.721f));
		redFlameSmashs.add(new Point3D(192.086f, 419.873f, 260.572f));
		redFlameSmashs.add(new Point3D(173.963f, 412.215f, 260.557f));
		redFlameSmashs.add(new Point3D(175.762f, 422.974f, 260.572f));
	}

	@Override
	protected void handleDespawned() {
		percents.clear();
		blueFlameSmashs.clear();
		redFlameSmashs.clear();
		cancelFlameBuffEvent();
		cancelAirEvent();
		cancelFlameSmashTask();
		super.handleDespawned();
	}

	@Override
	protected void handleBackHome() {
		canThink = true;
		addPercent();
		cancelFlameBuffEvent();
		cancelFlameSmashTask();
		cancelAirEvent();
		isHome.set(true);
		getPosition().getWorldMapInstance().getDoors().get(70).setOpen(true);
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		percents.clear();
		blueFlameSmashs.clear();
		redFlameSmashs.clear();
		cancelFlameBuffEvent();
		cancelFlameSmashTask();
		cancelAirEvent();
		getPosition().getWorldMapInstance().getDoors().get(70).setOpen(true);
		PacketSendUtility.broadcastMessage(getOwner(), 1500410);
		super.handleDied();
	}

}
