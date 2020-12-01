package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats
 */
@AIName("ahserion_troopers")
public class AhserionBalaurTroopersAI extends NpcAI {

	public AhserionBalaurTroopersAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		spawnAttackers();
		ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 25000);
	}

	private void spawnAttackers() {
		if (getOwner().getNpcId() == 297187) { // 297188 spawns already some attackers
			return;
		}
		if (getOwner().getSpawn() instanceof AhserionsFlightSpawnTemplate) {
			ThreadPoolManager.getInstance().schedule(() -> {
				if (AhserionRaid.getInstance().isStarted())
					AhserionRaid.getInstance().spawnStage(5, ((AhserionsFlightSpawnTemplate) getOwner().getSpawn()).getFaction());
			}, 6500);
		}
	}
}
