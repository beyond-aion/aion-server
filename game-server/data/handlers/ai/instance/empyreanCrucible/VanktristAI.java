package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

import ai.AggressiveNoLootNpcAI;

/**
 * @author w4terbomb
 */
@AIName("vanktrist")
public class VanktristAI extends AggressiveNoLootNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(100, 75, 50);

	private Future<?> phaseTask;

	public VanktristAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 100 -> PacketSendUtility.broadcastMessage(getOwner(), 1500232);
			case 75 -> {
				PacketSendUtility.broadcastMessage(getOwner(), 1500233);
				startPhaseTask();
			}
			case 50 -> PacketSendUtility.broadcastMessage(getOwner(), 1500235);
		}
	}

	private void startPhaseTask() {
		phaseTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isDead()) {
				cancelPhaseTask();
			} else {
				getOwner().queueSkill(19567, 46); // Gravitational Shift
			}
		}, 3000, 15000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 19567 && !isDead()) {
			List<Player> players = getAttackablePlayers();
			if (!players.isEmpty()) {
				Collections.shuffle(players);
				int count = Rnd.get(1, players.size());
				for (int i = 0; i < count; i++) {
					Player player = players.get(i);
					spawn(217804, player.getX(), player.getY(), player.getZ(), (byte) 0); // Weakened Dimensional Vortex
				}
			}
		}
	}

	private List<Player> getAttackablePlayers() {
		List<Player> players = new ArrayList<>();
		getKnownList().forEachPlayer(player -> {
			if (!player.isDead() && GeoService.getInstance().canSee(getOwner(), player))
				players.add(player);
		});
		return players;
	}

	private void cancelPhaseTask() {
		if (phaseTask != null && !phaseTask.isDone()) {
			phaseTask.cancel(true);
		}
	}

	@Override
	protected void handleDespawned() {
		cancelPhaseTask();
		super.handleDespawned();
	}

	@Override
	protected void handleBackHome() {
		cancelPhaseTask();
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleDied() {
		PacketSendUtility.broadcastMessage(getOwner(), 1500237);
		getPosition().getWorldMapInstance().getNpcs(217804).forEach(npc -> npc.getController().onDelete()); // Weakened Dimensional Vortex
		cancelPhaseTask();
		super.handleDied();
	}
}
