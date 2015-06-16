package ai.instance.empyreanCrucible;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 *
 * @author Luzien
 */
@AIName("takun_gojira")
public class TakunGojiraAI2 extends AggressiveNpcAI2 {
	
	private Npc counterpart;
	
	@Override
	public void handleSpawned() {
		super.handleSpawned();
			ThreadPoolManager.getInstance().schedule(new Runnable() {
			
				@Override
				public void run() {
					counterpart = getPosition().getWorldMapInstance().getNpc(getNpcId() == 217596 ? 217597 : 217596);
					if (counterpart != null)
						getAggroList().addHate(counterpart, 1000000);
			}
		}, 500);
	}
	
}