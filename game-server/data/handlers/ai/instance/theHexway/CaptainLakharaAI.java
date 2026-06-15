package ai.instance.theHexway;

import java.util.concurrent.ScheduledFuture;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.model.templates.ai.SummonGroup;
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
		getOwner().clearQueuedSkills();
		getOwner().queueSkill(17497, 65, 0);
		cancelDespawnTask();
		percent.getSummons().forEach(this::summonNpcWithSmoke);
		despawnTask = ThreadPoolManager.getInstance().schedule(() -> percent.getSummons().forEach(group -> despawnNpc(group.getNpcId())), 25000);
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
		if (despawnTask != null && !despawnTask.isDone())
			despawnTask.cancel(true);
	}

	private void despawnNpc(int npcId) {
		getPosition().getWorldMapInstance().getNpcs(npcId).forEach(npc -> npc.getController().deleteIfAliveOrCancelRespawn());
	}

	private void summonNpcWithSmoke(SummonGroup summon) {
		spawnHelpers(summon);
		Npc smoke = (Npc) spawn(282465, summon.getX(), summon.getY(), summon.getZ(), summon.getH());
		smoke.getController().delete();
	}

}
