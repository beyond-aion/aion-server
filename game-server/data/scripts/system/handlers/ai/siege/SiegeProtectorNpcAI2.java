package ai.siege;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.siegeservice.Siege;

/**
 * @author ATracer, Source
 */
@AIName("siege_protector")
public class SiegeProtectorNpcAI2 extends SiegeNpcAI2 {

	@Override
  public void handleBackHome() {
	  super.handleBackHome();
	  Siege<?> siege = SiegeService.getInstance().getSiege(((SiegeNpc) getOwner()).getSiegeId());
	  if (siege != null)
		 siege.getSiegeCounter().clearDamageCounters();
  }
}
