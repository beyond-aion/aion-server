package ai.instance.theShugoEmperorsVault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;

/**
 * @author Yeats
 *
 */
@AIName("watchman_hokuruki")
public class WatchmanHokuruki extends IDSweep_Bosses {

	protected List<Integer> percents = new ArrayList<>();
	private List<Npc> spawnedAdds = new ArrayList<>();
	private List<Integer> addStages = new ArrayList<>();
	
	
	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}
	
	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[]{100, 75, 50, 25, 15});
		Collections.addAll(addStages, new Integer[]{2, 3, 4});
	}
	
	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				switch (percent) {
					case 100:
						spawnAdds(1);
						break;
					case 75:
						int rnd = Rnd.get(0, (addStages.size()-1));
						spawnAdds(addStages.get(rnd));
						addStages.remove(rnd);
						break;
					case 50:
						int rnd2 = Rnd.get(0, (addStages.size()-1));
						spawnAdds(addStages.get(rnd2));
						addStages.remove(rnd2);
						break;
					case 25:
						int rnd3 = Rnd.get(0, (addStages.size()-1));
						spawnAdds(addStages.get(rnd3));
						addStages.remove(rnd3);
						break;
					case 15:
						spawnAdds(1);
						break;
				}
				percents.remove(percent);
				break;
			}
		}
	}
	
	private void spawnAdds(int stage) {
		switch (stage) {
			case 1:
				rndSpawn(235632, 4);
				break;
			case 2:
				spawn(236083, 472.8545f, 644.94f, 395.50546f, (byte) 113);
				spawn(235649, 473.7f, 648.9f, 395.55615f, (byte) 116);
				spawn(235649, 471.75f, 641f, 395.27322f, (byte) 116);
				break;
			case 3:
				spawn(236083, 490f, 655.1424f, 394.69073f, (byte) 84);
				spawn(235649, 485.812f, 656.14f, 395.375f, (byte) 90);
				spawn(235649, 493.59f, 652.62f, 395.41345f, (byte) 77);
				break;
			case 4:
				spawn(236083, 493f, 629.34f, 394.89963f, (byte) 41);
				spawn(235649, 496.56f, 631.78f, 395.20126f, (byte) 49);
				spawn(235649, 488.86f, 627.54f, 395.27853f, (byte) 38);
				break;
		}
		startHate();
	}
	
	private void startHate() {
		for (Npc npc : spawnedAdds) {
			if (npc != null && !npc.getLifeStats().isAlreadyDead()) {
				npc.getAggroList().addHate(getOwner().getAggroList().getMostHated(), 1);
			}
		}
	}
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}
	
	@Override
	protected void handleDied() {
		super.handleDied();
		percents.clear();
		getOwner().getController().delete();
	}
	
	private void rndSpawn(int npcId, int count) {
	  for (int i = 0; i < count; i++) {
		 SpawnTemplate template = rndSpawnInRange(npcId);
		 Npc npc = (Npc) SpawnEngine.spawnObject(template, getPosition().getInstanceId());
		 spawnedAdds.add(npc);
		 }
   }
	
	private SpawnTemplate rndSpawnInRange(int npcId) {
	  float direction = Rnd.get(0, 199) / 100f;
	  int range = Rnd.get(3, 5);
	  float x1 = (float) (Math.cos(Math.PI * direction) * range);
	  float y1 = (float) (Math.sin(Math.PI * direction) * range);
	  return SpawnEngine.addNewSingleTimeSpawn(getPosition().getMapId(), npcId, getPosition().getX() + x1, getPosition().getY()
			  + y1, getPosition().getZ(), getPosition().getHeading());
   }

}
