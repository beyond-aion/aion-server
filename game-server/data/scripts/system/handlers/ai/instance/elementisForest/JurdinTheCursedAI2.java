package ai.instance.elementisForest;

import java.util.concurrent.Future;

import ai.SummonerAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.ai.Percentage;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Luzien
 */
@AIName("jurdin")
public class JurdinTheCursedAI2 extends SummonerAI2 {

	private boolean isStart;
	private Future<?> task;

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (getLifeStats().getHpPercentage() <= 90 && !isStart) {
			isStart = true;
			startTask();
		}
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	public void handleBackHome() {
		super.handleBackHome();
		isStart = false;
		cancelTask();
	}

	@Override
	public void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	public void handleIndividualSpawnedSummons(Percentage percent) {
		int npcId1 = 0;
		int count1 = 0;
		int npcId2 = 0;
		int count2 = 0;
		switch (percent.getPercent()) {
			case 80:
				npcId1 = 282190;
				count1 = 15;
				break;
			case 60:
				npcId1 = 282194;
				count1 = 24;
				npcId2 = 282195;
				count2 = 3;
				break;
			case 40:
				npcId1 = 282190;
				count1 = 10;
				npcId2 = 282193;
				count2 = 2;
				break;
			case 20:
				npcId1 = 282195;
				count1 = 7;
				npcId2 = 282197;
				count2 = 5;
				break;
		}
		randomSpawnHelpers(npcId1, count1);
		if (npcId2 != 0 && count2 != 0) {
			randomSpawnHelpers(npcId2, count2);
		}
	}

	private void startTask() {

		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead()) {
					cancelTask();
				} else {
					spawnShadows();
				}
			}
		}, 0, 60000);
	}

	private void spawnShadows() {
		spawnFlowers();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead() || !isStart) {
					return;
				}
				WorldPosition p = getPosition();
				if (p != null) {
					WorldMapInstance instance = p.getWorldMapInstance();
					if (instance != null) {
						for (int i = 0; i < 7; i++) {
							SpawnTemplate temp = rndSpawnInRange(282201, 10);
							VisibleObject o = SpawnEngine.spawnObject(temp, getPosition().getInstanceId());
							addHelpersSpawn(o.getObjectId());
						}
					}
				}
			}
		}, 5000);
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}

	private void randomSpawnHelpers(int npcId, int count) {

		for (int i = 0; i < count; i++) {
			float x1 = Rnd.get(20);
			float y1 = Rnd.get(10);
			switch (Rnd.get(1, 3)) {
				case 1:
					spawn(npcId, 450 + x1, 750 + y1, 131f, (byte) 0);
					break;
				case 2:
					spawn(npcId, 470 + x1, 815 + y1, 131f, (byte) 0);
					break;
				case 3:
					spawn(npcId, 470 + x1, 760 + y1, 131f, (byte) 0);
					break;
			}
		}
	}

	private void spawnFlowers() {
		WorldPosition p = getPosition();
		if (p != null) {
			WorldMapInstance instance = p.getWorldMapInstance();
			if (instance != null) {
				spawn(282440, 460.795f, 801.471f, 130.759f, (byte) 0);
				spawn(282440, 485.118f, 790.683f, 129.668f, (byte) 0);
				spawn(282440, 474.227f, 777.928f, 128.875f, (byte) 0);
				spawn(282440, 475.009f, 783.226f, 128.875f, (byte) 0);
				spawn(282440, 459.194f, 797.047f, 130.491f, (byte) 0);
				spawn(282440, 484.584f, 791.999f, 129.750f, (byte) 0);
				spawn(282440, 485.502f, 803.050f, 130.472f, (byte) 0);
				spawn(282440, 486.006f, 804.645f, 130.540f, (byte) 0);
				spawn(282440, 412.354f, 800.439f, 131.176f, (byte) 0);
				spawn(282440, 474.676f, 816.744f, 131.281f, (byte) 0);
				spawn(282440, 412.354f, 800.439f, 131.176f, (byte) 0);
				spawn(282440, 467.355f, 811.444f, 131.261f, (byte) 0);
				spawn(282440, 463.336f, 798.282f, 130.309f, (byte) 0);
			}
		}
	}

}
