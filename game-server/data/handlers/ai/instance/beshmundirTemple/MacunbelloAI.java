package ai.instance.beshmundirTemple;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.controllers.attack.AggroTarget;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * Macunbello, retail AI IDCT_Boss_LichKing (all variants except 216164) and IDCTH_Boss_LichKing (216164).
 * <ul>
 * <li>Tide of Darkness and Shockwave are handled by the skill list (see npc_skills/instances/300170000_Beshmundir_Temple.xml).</li>
 * <li>Absorb Energy of Darkness is cast once on a random attacker below each HP threshold.</li>
 * <li>Summons two (above 50% HP) or four (below 50% HP) Right Hands every 30/40s (first after 20s). Whenever a Right Hand casts Curse of Souls,
 * Macunbello follows up with Devour Soul on the cursed player.</li>
 * <li>216164 additionally summons a Right Hand at its own position with a 5% chance when attacked (at most once between two Tide of Darkness).</li>
 * <li>The door to his chamber is closed while he is in combat.</li>
 * </ul>
 *
 * @author Estrayl
 */
@AIName("macunbello")
public class MacunbelloAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	public static final int EVENT_SOUL_CURSED = 6980;
	private static final int DEVOUR_SOUL = 19051;
	private static final int TIDE_OF_DARKNESS = 19053;
	private static final int ABSORB_ENERGY_OF_DARKNESS = 19054;
	private static final int RIGHT_HAND = 281698;
	private static final int RIGHT_HAND_HARD = 281775;
	private static final int CHAMBER_DOOR = 467;
	private static final float[][] RIGHT_HAND_POSITIONS = { { 955.54f, 160.44f, 242.3f }, { 1004.12f, 159.32f, 242.3f }, { 952f, 110.41f, 243.4f },
		{ 1007.07f, 110.33f, 243.4f } };

	private final AtomicBoolean isStarted = new AtomicBoolean();
	private final AtomicBoolean canSummonRightHandOnAttack = new AtomicBoolean(true);
	private final AtomicInteger encounterId = new AtomicInteger();
	private final List<Npc> rightHands = new CopyOnWriteArrayList<>();
	private final HpPhases absorbPhases;
	private volatile Future<?> summonTask;

	public MacunbelloAI(Npc owner) {
		super(owner);
		absorbPhases = owner.getNpcId() == 216164 ? new HpPhases(90, 80, 70, 60, 50, 40, 30, 20, 10) : new HpPhases(90, 70, 50, 30, 10);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		startEncounter();
		absorbPhases.tryEnterNextPhase(this);
		if (isHardBase() && Rnd.chance() < 5 && canSummonRightHandOnAttack.compareAndSet(true, false))
			spawnRightHand(RIGHT_HAND_HARD, getOwner().getX(), getOwner().getY(), getOwner().getZ(), encounterId.get());
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		startEncounter();
	}

	private void startEncounter() {
		if (!isStarted.compareAndSet(false, true))
			return;
		getPosition().getWorldMapInstance().setDoorState(CHAMBER_DOOR, false);
		PacketSendUtility.broadcastMessage(getOwner(), 1500060);
		scheduleRightHandSummon(20000, encounterId.get());
	}

	private boolean isHardBase() {
		return getNpcId() == 216164;
	}

	/**
	 * @return The level of Tide of Darkness, Shockwave and Absorb Energy of Darkness for this variant (retail skill list).
	 */
	private int getSkillLevel() {
		return switch (getNpcId()) {
			case 216164 -> 22;
			case 216245 -> 20;
			case 216733 -> 16;
			case 216736 -> 15;
			case 216734 -> 10;
			case 216735, 216737 -> 5;
			default -> 1; // 216738
		};
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		getOwner().queueSkill(ABSORB_ENERGY_OF_DARKNESS, getSkillLevel(), 0, NpcSkillTargetAttribute.RANDOM);
	}

	private void scheduleRightHandSummon(int delay, int id) {
		summonTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isDead() || encounterId.get() != id)
				return;
			boolean belowHalf = getLifeStats().getHpPercentage() <= 50;
			PacketSendUtility.broadcastMessage(getOwner(), 1500061);
			for (int i = 0; i < (belowHalf ? 4 : 2); i++)
				spawnRightHand(RIGHT_HAND, RIGHT_HAND_POSITIONS[i][0], RIGHT_HAND_POSITIONS[i][1], RIGHT_HAND_POSITIONS[i][2], id);
			if (encounterId.get() == id)
				scheduleRightHandSummon(belowHalf ? 40000 : 30000, id);
		}, delay);
	}

	private void spawnRightHand(int npcId, float x, float y, float z, int id) {
		Npc rightHand = (Npc) spawn(npcId, x, y, z, getOwner().getHeading());
		rightHands.add(rightHand);
		if (encounterId.get() != id) {
			rightHands.remove(rightHand);
			rightHand.getController().delete();
			return;
		}
		Creature target = getAggroList().getTarget(AggroTarget.RANDOM);
		if (target != null)
			rightHand.getAggroList().addHate(target, 1);
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		super.onStartUseSkill(skillTemplate, skillLevel);
		if (skillTemplate.getSkillId() == TIDE_OF_DARKNESS)
			canSummonRightHandOnAttack.set(true);
	}

	@Override
	protected void handleCustomEvent(int eventId, Object... args) {
		if (eventId == EVENT_SOUL_CURSED && args.length > 0 && args[0] instanceof Creature cursed)
			devourSoul(cursed, 0);
		else
			super.handleCustomEvent(eventId, args);
	}

	private void devourSoul(Creature target, int attempt) {
		if (isDead() || !isStarted.get() || target.isDead())
			return;
		if (getOwner().isCasting()) { // retail waits for the current cast to finish
			if (attempt < 10)
				ThreadPoolManager.getInstance().schedule(() -> devourSoul(target, attempt + 1), 500);
			return;
		}
		PacketSendUtility.broadcastMessage(getOwner(), 1500062);
		SkillEngine.getInstance().getSkill(getOwner(), DEVOUR_SOUL, 1, target).useSkill();
	}

	private void endEncounter() {
		encounterId.incrementAndGet();
		isStarted.set(false);
		Future<?> task = summonTask;
		if (task != null)
			task.cancel(false);
		for (Npc rightHand : rightHands) {
			rightHands.remove(rightHand);
			if (rightHand.isSpawned())
				rightHand.getController().delete();
		}
		absorbPhases.reset();
		canSummonRightHandOnAttack.set(true);
		getPosition().getWorldMapInstance().setDoorState(CHAMBER_DOOR, true);
	}

	@Override
	protected void handleBackHome() {
		endEncounter();
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		endEncounter();
		PacketSendUtility.broadcastMessage(getOwner(), 1500063);
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		endEncounter();
		super.handleDespawned();
	}
}
