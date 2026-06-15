package ai.instance.rentusBase;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author xTz, Yeats, Estrayl
 */
@AIName("brigade_general_vasharti")
public class BrigadeGeneralVashartiAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(75, 50, 25, 10);
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private AtomicBoolean isInFlameShowerEvent = new AtomicBoolean();
	private Future<?> enrageSchedule, flameShieldBuffSchedule, seaOfFireSpawnTask;

	public BrigadeGeneralVashartiAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			getPosition().getWorldMapInstance().setDoorState(70, false);
			enrageSchedule = ThreadPoolManager.getInstance().schedule(this::handleEnrageEvent, 10, TimeUnit.MINUTES);
			scheduleFlameShieldBuffEvent(5000);
		}
		if (!isInFlameShowerEvent.get())
			hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		cancelTasks(flameShieldBuffSchedule);
		getOwner().clearQueuedSkills();
		getOwner().queueSkill(20532, 1, 10000); // off (skill name)
	}

	private void scheduleFlameShieldBuffEvent(int delay) {
		flameShieldBuffSchedule = ThreadPoolManager.getInstance().schedule(() -> getOwner().queueSkill(20530 + Rnd.get(0, 1), 60), delay);
	}

	private void handleEnrageEvent() {
		getOwner().clearQueuedSkills();
		getOwner().queueSkill(19962, 1, 15000); // Purple Flame Weapon
		getOwner().queueSkill(19907, 1, 0); // Chastise
	}

	private void handleSeaOfFireEvent() {
		int percent = getLifeStats().getHpPercentage();
		int npcId = percent <= 70 ? percent <= 40 ? 283012 : 283011 : 283010;

		spawn(npcId, 188.33f, 414.61f, 260.61f, (byte) 244); // FX
		spawn(283007, 188.33f, 414.61f, 260.61f, (byte) 0); // de-buff

		seaOfFireSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			int smashCount = (npcId - 283007) * 5 + 1; // 15, 20, 25
			for (int i = 2; i < smashCount; i++) {
				rndSpawnInRange(i % 2 == 0 ? 283008 : 283009, 0, 29);
			}
		}, 750, 7100);
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20534:
				handleSeaOfFireEvent();
				break;
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 19907: // repeat until reset
				getOwner().queueSkill(19907, 1, 0); // Chastise
				break;
			case 20530:
			case 20531:
				WorldMapInstance instance = getPosition().getWorldMapInstance();
				if (instance != null) {
					if (instance.getNpc(283000) == null)
						spawn(283000, 171.330f, 417.57f, 261f, (byte) 116);
					if (instance.getNpc(283001) == null)
						spawn(283001, 205.280f, 410.53f, 261f, (byte) 56);
				}
				scheduleFlameShieldBuffEvent(33000);
				break;
			case 20532:
				EmoteManager.emoteStopAttacking(getOwner());
				getOwner().clearQueuedSkills();
				ThreadPoolManager.getInstance().schedule(() -> {
					WalkManager.startForcedWalking(this, 188.17f, 414.06f, 260.75488f);
					getOwner().setState(CreatureState.ACTIVE, true);
					PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
				}, 800);
				break;
			case 20533:
				setStateIfNot(AIState.FIGHT);
				SkillEngine.getInstance().getSkill(getOwner(), 20534, 1, getOwner()).useSkill(); // Sea of Fire
				break;
		}

	}

	@Override
	public void onEffectEnd(Effect effect) {
		if (effect != null && effect.getSkillId() == 20534 && isInFlameShowerEvent.compareAndSet(true, false)) {
			cancelTasks(seaOfFireSpawnTask);
			getKnownList().forEachNpc(n -> {
				switch (getNpcId()) {
					case 283010:
					case 283011:
					case 283012:
						n.getController().delete();
						break;
				}
			});
			scheduleFlameShieldBuffEvent(10000);
			getOwner().getAggroList().addHate((Creature) getTarget(), 1000);
		}
	}

	@Override
	public boolean isDestinationReached() {
		if (getState() == AIState.FORCED_WALKING && PositionUtil.getDistance(getOwner().getX(), getOwner().getY(), 188.17f, 414.06f) <= 1f
			&& isInFlameShowerEvent.compareAndSet(false, true)) {
			SkillEngine.getInstance().getSkill(getOwner(), 20533, 1, getOwner()).useSkill(); // off (skill name)
		}
		return super.isDestinationReached();
	}

	private void clearSpawns() {
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

	private void deleteNpcs(List<Npc> npcs) {
		npcs.stream().filter(Objects::nonNull).forEach(npc -> npc.getController().delete());
	}

	private void cancelTasks(Future<?>... tasks) {
		for (Future<?> task : tasks)
			if (task != null && !task.isCancelled())
				task.cancel(true);
	}

	@Override
	protected void handleDespawned() {
		cancelTasks(enrageSchedule, flameShieldBuffSchedule, seaOfFireSpawnTask);
		clearSpawns();
		super.handleDespawned();
	}

	@Override
	protected void handleBackHome() {
		isHome.set(true);
		getPosition().getWorldMapInstance().setDoorState(70, true);
		cancelTasks(enrageSchedule, flameShieldBuffSchedule, seaOfFireSpawnTask);
		clearSpawns();
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleDied() {
		getPosition().getWorldMapInstance().setDoorState(70, true);
		cancelTasks(enrageSchedule, flameShieldBuffSchedule, seaOfFireSpawnTask);
		PacketSendUtility.broadcastMessage(getOwner(), 1500410);
		clearSpawns();
		super.handleDied();
	}
}
