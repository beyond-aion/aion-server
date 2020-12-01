package ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.model.templates.ai.SummonGroup;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author xTz
 */
@AIName("summoner")
public class SummonerAI extends AggressiveNpcAI {

	private final List<Integer> spawnedNpc = new ArrayList<>();
	private List<Percentage> percentage = Collections.emptyList();
	private volatile int spawnedPercent = 0;

	public SummonerAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		removeAndResetHelperSpawns();
		percentage.clear();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		removeAndResetHelperSpawns();
	}

	@Override
	protected void handleNotAtHome() {
		super.handleNotAtHome();
		if (getState() == AIState.WALKING)
			removeAndResetHelperSpawns();
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		percentage = new ArrayList<>(DataManager.AI_DATA.getAiTemplate(getNpcId()).getSummons().getPercentage());
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		removeAndResetHelperSpawns();
		percentage.clear();
	}

	private void removeAndResetHelperSpawns() {
		synchronized (spawnedNpc) {
			for (Integer object : spawnedNpc) {
				VisibleObject npc = World.getInstance().findVisibleObject(object);
				if (npc != null && npc.isSpawned()) {
					npc.getController().delete();
				}
			}
			spawnedNpc.clear();
		}
		spawnedPercent = 0;
	}

	protected void addHelpersSpawn(int objId) {
		synchronized (spawnedNpc) {
			spawnedNpc.add(objId);
		}
	}

	private void checkPercentage(int hpPercentage) {
		for (Percentage percent : percentage) {
			if (spawnedPercent != 0 && spawnedPercent <= percent.getPercent()) {
				continue;
			}

			if (hpPercentage <= percent.getPercent()) {
				int skill = percent.getSkillId();
				if (skill != 0)
					AIActions.useSkill(this, skill);

				if (percent.isIndividual()) {
					handleIndividualSpawnedSummons(percent);
				} else if (percent.getSummons() != null) {
					handleBeforeSpawn(percent);
					for (SummonGroup summonGroup : percent.getSummons()) {
						final SummonGroup sg = summonGroup;
						ThreadPoolManager.getInstance().schedule(() -> spawnHelpers(sg), summonGroup.getSchedule());
					}
				}
				spawnedPercent = percent.getPercent();
			}
		}
	}

	protected void spawnHelpers(SummonGroup summonGroup) {
		if (!isDead() && checkBeforeSpawn()) {
			int count = Rnd.get(summonGroup.getMinCount(), summonGroup.getMaxCount());
			for (int i = 0; i < count; i++) {
				VisibleObject npc;
				if (summonGroup.getDistance() != 0)
					npc = rndSpawnInRange(summonGroup.getNpcId(), summonGroup.getDistance());
				else
					npc = spawn(summonGroup.getNpcId(), summonGroup.getX(), summonGroup.getY(), summonGroup.getZ(), summonGroup.getH());

				addHelpersSpawn(npc.getObjectId());
			}
			handleSpawnFinished(summonGroup);
		}
	}

	protected boolean checkBeforeSpawn() {
		return true;
	}

	protected void handleBeforeSpawn(Percentage percent) {
	}

	protected void handleSpawnFinished(SummonGroup summonGroup) {
	}

	protected void handleIndividualSpawnedSummons(Percentage percent) {
	}

}
