package ai.instance.theHexway;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.model.templates.ai.SummonGroup;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.SummonerAI;

/**
 * @author Sykra
 */
@AIName("captain_lakhara")
public class CaptainLakharaAI extends SummonerAI {

	private ScheduledFuture<?> despawnTask;

	public CaptainLakharaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleIndividualSpawnedSummons(Percentage percent) {
		SkillEngine.getInstance().getSkill(getOwner(), 17497, 65, getRandomTarget()).useNoAnimationSkill();
		ThreadPoolManager.getInstance().schedule(() -> SkillEngine.getInstance().getSkill(getOwner(), 17039, 65, getRandomTarget()).useNoAnimationSkill(),
			1000);
		for (SummonGroup summon : percent.getSummons()) {
			cancelDespawnTask();
			summonNpcWithSmoke(summon);
		}
		despawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			for (SummonGroup summon : percent.getSummons()) {
				despawnNpc(summon.getNpcId());
			}
		}, 25 * 1000);
	}

	private Player getRandomTarget() {
		List<Player> players = new ArrayList<>();
		for (Player player : getKnownList().getKnownPlayers().values())
			if (!player.isDead() && PositionUtil.isInRange(player, getOwner(), 50))
				players.add(player);

		return players.isEmpty() ? null : Rnd.get(players);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelDespawnTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelDespawnTask();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelDespawnTask();
	}

	private void cancelDespawnTask() {
		if (despawnTask != null && !despawnTask.isCancelled())
			despawnTask.cancel(true);
	}

	private void despawnNpc(int npcId) {
		List<Npc> npcs = getPosition().getWorldMapInstance().getNpcs(npcId);
		for (Npc npc : npcs)
			if (!npc.isDead())
				NpcActions.delete(npc);
	}

	private void summonNpcWithSmoke(SummonGroup summon) {
		spawnHelpers(summon);
		Npc smoke = (Npc) spawn(282465, summon.getX(), summon.getY(), summon.getZ(), summon.getH());
		NpcActions.delete(smoke);
	}

}
