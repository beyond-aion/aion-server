package ai.instance.drakenspire;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Estrayl
 */
@AIName("drakenspire_seal_guardian")
public class SealGuardianAI extends AggressiveNoLootNpcAI {

	private final AtomicBoolean isIdling = new AtomicBoolean(true);
	private Future<?> idleTimer;

	public SealGuardianAI(Npc owner) {
		super(owner);
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_WIND;
	}

	private Player getLastAttacker() {
		AggroInfo lastAttacker = getAggroList().getFinalDamageList(false).stream()
			.filter(ai -> ai.getAttacker() instanceof Player && !((Player) ai.getAttacker()).isDead())
			.max(Comparator.comparingLong(AggroInfo::getLastInteractionTime)).orElse(null);
		return lastAttacker != null ? (Player) lastAttacker.getAttacker() : null;
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21882 && skillLevel == 57)
			PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(ChatType.NPC, getOwner(), 1501357)); // Intruder…
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21882)
			addHateToRandomTarget();
	}

	private void addHateToRandomTarget() {
		List<AggroInfo> attackingPlayers = getAggroList().getList().stream().filter(ai -> ai.getAttacker() instanceof Player player && !player.isDead())
			.toList();
		AggroInfo aggroInfo = Rnd.get(attackingPlayers);
		if (aggroInfo != null)
			aggroInfo.addHate(10000);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		getOwner().getGameStats().setNextSkillDelay(0);
		startIdleTimer();
		ThreadPoolManager.getInstance().schedule(this::spawnSpecter, 1000);
	}

	private void startIdleTimer() {
		idleTimer = ThreadPoolManager.getInstance().schedule(this::despawn, 60, TimeUnit.SECONDS);
	}

	private void despawn() {
		PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(ChatType.NPC, getOwner(), 1501358)); // Teleport…
		notifyBeritra(2);
		AIActions.deleteOwner(this);
	}

	private void spawnSpecter() {
		switch (getNpcId()) {
			case 855460 -> spawn(855452, 141.618f, 498.609f, 1749.590f, (byte) 30);
			case 855461 -> spawn(855454, 172.045f, 509.876f, 1749.590f, (byte) 45);
			case 855462 -> spawn(855456, 172.142f, 525.665f, 1749.590f, (byte) 75);
			case 855463 -> spawn(855458, 142.027f, 536.810f, 1749.590f, (byte) 90);
			// Will only spawn during dragon phase
			case 855464, 855465, 855466, 855467, 855468, 855469 -> {
				spawn(855452, 141.618f, 498.609f, 1749.590f, (byte) 30);
				spawn(855454, 172.045f, 509.876f, 1749.590f, (byte) 45);
				spawn(855456, 172.142f, 525.665f, 1749.590f, (byte) 75);
				spawn(855458, 142.027f, 536.810f, 1749.590f, (byte) 90);
			}
		}
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (isIdling.compareAndSet(true, false))
			cancelTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		despawn();
	}

	@Override
	protected void handleDied() {
		Player lastAttacker = getLastAttacker();
		if (lastAttacker != null)
			SkillEngine.getInstance().applyEffectDirectly(21625, getOwner(), lastAttacker);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(ChatType.NPC, getOwner(), 1501359)); // I shall… curse you…

		notifyBeritra(1);
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		despawnSpecters();
		super.handleDespawned();
	}

	private void cancelTask() {
		if (idleTimer != null && !idleTimer.isDone())
			idleTimer.cancel(true);
	}

	private void despawnSpecters() {
		int[] specterIds = switch (getNpcId()) {
			case 855460 -> new int[] { 855452 };
			case 855461 -> new int[] { 855454 };
			case 855462 -> new int[] { 855456 };
			case 855463 -> new int[] { 855458 };
			default -> new int[] { 855452, 855454, 855456, 855458 };
		};

		getPosition().getWorldMapInstance().getNpcs(specterIds).forEach(npc -> npc.getController().deleteIfAliveOrCancelRespawn());
	}

	private void notifyBeritra(int eventId) {
		List<Npc> possibleBeritras = getPosition().getWorldMapInstance().getNpcs(236244, 236245, 236246);
		if (!possibleBeritras.isEmpty()) {
			possibleBeritras.getFirst().getAi().onCustomEvent(eventId); // 1 = Death, 2 = back home | 60s idle
		}
	}
}
