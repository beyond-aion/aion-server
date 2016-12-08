package ai.events;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.time.ServerTime;

import ai.AggressiveNpcAI2;

/**
 * @author Bobobear
 */
@AIName("rvr_guard")
public class RvrGuardAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		// add player to event list for additional reward
		if (creature instanceof Player && getPosition().getMapId() == 600010000) {
			int hour = ServerTime.now().getHour();
			if (hour >= 19 && hour <= 23) {
				Npc bossAsmo = getPosition().getWorldMapInstance().getNpc(220948);
				Npc bossElyos = getPosition().getWorldMapInstance().getNpc(220949);
				if (bossAsmo != null && bossElyos != null) {
					SiegeService.getInstance().checkRvrPlayerOnEvent((Player) creature);
				}
			}
		}
	}
}
