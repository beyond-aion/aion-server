package ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.model.templates.ai.SummonGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author xTz
 */
@AIName("summoner")
public class SummonerAI extends AggressiveNpcAI {

	private final List<Integer> spawnedNpc = new ArrayList<>();
	private List<Percentage> percentage = Collections.emptyList();
	private int spawnedPercent = 0;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		removeHelpersSpawns();
		percentage.clear();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		removeHelpersSpawns();
		spawnedPercent = 0;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		percentage = new ArrayList<>(DataManager.AI_DATA.getAiTemplate(getNpcId()).getSummons().getPercentage());
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		removeHelpersSpawns();
		percentage.clear();
	}

	private void removeHelpersSpawns() {
		synchronized (spawnedNpc) {
			for (Integer object : spawnedNpc) {
				VisibleObject npc = World.getInstance().findVisibleObject(object);
				if (npc != null && npc.isSpawned()) {
					npc.getController().delete();
				}
			}
			spawnedNpc.clear();
		}
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
				if (skill != 0) {
					AIActions.useSkill(this, skill);
				}

				if (percent.isIndividual()) {
					handleIndividualSpawnedSummons(percent);
				} else if (percent.getSummons() != null) {
					handleBeforeSpawn(percent);
					for (SummonGroup summonGroup : percent.getSummons()) {
						final SummonGroup sg = summonGroup;
						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								spawnHelpers(sg);
							}
						}, summonGroup.getSchedule());

					}
				}
				spawnedPercent = percent.getPercent();
			}
		}
	}

	protected void spawnHelpers(SummonGroup summonGroup) {
		if (!isAlreadyDead() && checkBeforeSpawn()) {
			int count = 0;
			if (summonGroup.getCount() != 0) {
				count = summonGroup.getCount();
			} else {
				count = Rnd.get(summonGroup.getMinCount(), summonGroup.getMaxCount());
			}
			for (int i = 0; i < count; i++) {
				SpawnTemplate summon = null;
				if (summonGroup.getDistance() != 0) {
					summon = rndSpawnInRange(summonGroup.getNpcId(), summonGroup.getDistance());
				} else {
					summon = SpawnEngine.newSingleTimeSpawn(getPosition().getMapId(), summonGroup.getNpcId(), summonGroup.getX(), summonGroup.getY(),
						summonGroup.getZ(), summonGroup.getH());
				}
				VisibleObject npc = SpawnEngine.spawnObject(summon, getPosition().getInstanceId());
				addHelpersSpawn(npc.getObjectId());
			}
			handleSpawnFinished(summonGroup);
		}
	}

	protected SpawnTemplate rndSpawnInRange(int npcId, float distance) {
		float direction = Rnd.get(0, 199) / 100f;
		float x = (float) (Math.cos(Math.PI * direction) * distance);
		float y = (float) (Math.sin(Math.PI * direction) * distance);
		return SpawnEngine.newSingleTimeSpawn(getPosition().getMapId(), npcId, getPosition().getX() + x, getPosition().getY() + y,
			getPosition().getZ(), getPosition().getHeading());
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
