package ai.worlds.inggison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("sematariux")
public class SematariuxAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(90, 70, 50, 30, 20, 10, 5);
	private AtomicInteger deadThunderShields = new AtomicInteger();
	private AtomicBoolean isEggEventActive = new AtomicBoolean();
	// private long shieldRemovalStamp;

	public SematariuxAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> SkillEngine.getInstance().applyEffectDirectly(19186, getOwner(), getOwner()), 3000);
		spawnShieldNpcs();
	}

	private void spawnShieldNpcs() {
		spawnAndObserveNpc(281931, new WorldPosition(210050000, 101.31f, 2136.59f, 441.456f, (byte) 0));
		spawnAndObserveNpc(281931, new WorldPosition(210050000, 117.88f, 2097.47f, 440.581f, (byte) 0));
		spawnAndObserveNpc(281931, new WorldPosition(210050000, 126.64f, 2175.42f, 441.662f, (byte) 0));
		spawnAndObserveNpc(281931, new WorldPosition(210050000, 160.65f, 2099.39f, 439.625f, (byte) 0));
		spawnAndObserveNpc(281931, new WorldPosition(210050000, 169.32f, 2175.65f, 441.708f, (byte) 0));
		spawnAndObserveNpc(281931, new WorldPosition(210050000, 187.13f, 2137.05f, 441.185f, (byte) 0));

		spawn(282123, 121.00f, 2119.01f, 441.626f, (byte) 0, 1463);
		spawn(282123, 142.35f, 2110.25f, 441.213f, (byte) 0, 1464);
		spawn(282123, 161.46f, 2119.23f, 441.356f, (byte) 0, 1757);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 90, 70, 50, 30, 20, 10 -> spawnTornado();
			case 5 -> getOwner().queueSkill(18730, 1, 3000); // Berserk State
		}
	}

	@Override
	public boolean isDestinationReached() {
		if (getState() == AIState.FORCED_WALKING) {
			if (PositionUtil.isInRange(getOwner(), 141.77f, 2142.31f, 440.0f, 4f) && isEggEventActive.compareAndSet(false, true)) {
				Npc egg = (Npc) spawn(281451, 141.77f, 2142.31f, 440.0f, (byte) 0);
				ThreadPoolManager.getInstance().schedule(() -> SkillEngine.getInstance().getSkill(getOwner(), 18726, 1, egg).useNoAnimationSkill(), 2500);
			}
		}
		return super.isDestinationReached();
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 18726: // Extract Essence
				isEggEventActive.set(false);
				setStateIfNot(AIState.FIGHT);
				AIActions.targetCreature(this, getAggroList().getMostHated());
				getMoveController().moveToTargetObject();
				break;
			case 19178: // Deadly Bolt
				Collection<Player> knownPlayers = getKnownList().getKnownPlayers().values();
				List<Integer> targetedPlayers = new ArrayList<>();
				while (targetedPlayers.size() < knownPlayers.size() * 0.2f) {
					for (Player p : knownPlayers) {
						if (!targetedPlayers.contains(p.getObjectId()) && Rnd.nextBoolean()) {
							spawn(281452, p.getX(), p.getY(), p.getZ(), (byte) 0);
							targetedPlayers.add(p.getObjectId());
						}
					}
				}
				break;
			case 19182: // Ear Piercing Shriek
				// ThreadPoolManager.getInstance().schedule(() -> {
				// PacketSendUtility.broadcastPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_LF4_DRAMATA_LAY_EGG());
				// WalkManager.startForcedWalking(this, 141.77f, 2142.31f, 440.0f);
				// }, 1500);
				break;
		}
	}

	@Override
	public void onEffectEnd(Effect effect) {
		if (effect.getSkillId() == 19186) {
			// shieldRemovalStamp = System.currentTimeMillis();
			despawnNpcs(Arrays.asList(282123));
			PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_LF4_DRAMATA_AWAKENING());
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		// if (System.currentTimeMillis() - shieldRemovalStamp >= TimeUnit.MINUTES.toMillis(5)) {
		// SkillEngine.getInstance().applyEffectDirectly(19186, getOwner(), getOwner());
		// deadThunderShields.set(0);
		// ThreadPoolManager.getInstance().schedule(this::spawnShieldNpcs, Rnd.get(120, 300), TimeUnit.MINUTES);
		// }
		hpPhases.reset();
		despawnNpcs(Arrays.asList(281453, 281451, 281931, 281932, 281933));
	}

	@Override
	protected void handleDied() {
		despawnNpcs(Arrays.asList(281453, 281451, 281931, 281932, 281933));
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		despawnNpcs(Arrays.asList(281453, 281451, 281931, 281932, 281933));
		super.handleDespawned();
	}

	private void despawnNpcs(List<Integer> npcIds) {
		getKnownList().getKnownObjects().values().forEach(o -> {
			if (o instanceof Npc && npcIds.contains(((Npc) o).getNpcId()))
				o.getController().delete();
		});
	}

	private void spawnTornado() {
		Player rndPlayer = Rnd.get(getKnownList().getKnownPlayers().values().stream()
			.filter(p -> !p.isDead() && PositionUtil.isInRange(p, getOwner(), 80)).collect(Collectors.toList()));
		if (rndPlayer != null)
			spawn(281453, rndPlayer.getX(), rndPlayer.getY(), rndPlayer.getZ(), (byte) 0);
	}

	private void handleObservedNpcDied(Npc npc) {
		switch (npc.getNpcId()) {
			case 281931:
				spawnAndObserveNpc(281932, npc.getPosition());
				spawnAndObserveNpc(281932, npc.getPosition());
				break;
			case 281932:
				spawnAndObserveNpc(281933, npc.getPosition());
				spawnAndObserveNpc(281933, npc.getPosition());
				break;
			case 281933:
				if (deadThunderShields.incrementAndGet() >= 24)
					getOwner().getEffectController().removeEffect(19186); // Protective Slumber
				break;
		}
	}

	private void spawnAndObserveNpc(int npcId, WorldPosition pos) {
		Npc npc = (Npc) spawn(npcId, pos.getX() + Rnd.get(-5, 5), pos.getY() + Rnd.get(-3, 3), pos.getZ() + 0.4f, (byte) 0);
		npc.getObserveController().addObserver(new ActionObserver(ObserverType.DEATH) {

			@Override
			public void died(Creature creature) {
				handleObservedNpcDied(npc);
			}
		});
	}
}
