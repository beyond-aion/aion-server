package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionInstance;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats
 *
 */
@AIName("ahserion_troopers")
public class AhserionBalaurTroopersAI2 extends NpcAI2 {

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		spawnAttackers();
		scheduleDespawn();
	}
	
	private void spawnAttackers() {
		if (getOwner().getNpcId() == 297187) { //297188 spawns already some attackers
			return;
		}
		if (getOwner().getSpawn() instanceof AhserionsFlightSpawnTemplate) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					if (AhserionInstance.getInstance().isStarted() 
						&& AhserionInstance.getInstance().isTeamNotEliminated(((AhserionsFlightSpawnTemplate) getOwner().getSpawn()).getTeam())) {
							AhserionInstance.getInstance().spawnStage(5, ((AhserionsFlightSpawnTemplate) getOwner().getSpawn()).getTeam());
						}
				}
			}, 6500);
		}
	}
	
	private void scheduleDespawn() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (getOwner() != null)
					getOwner().getController().onDelete();
			}
		}, 25000);
	}
}
