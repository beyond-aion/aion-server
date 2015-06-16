package ai.portals;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.SourceLocation;
import com.aionemu.gameserver.services.SiegeService;


/**
 * @author Cheatkiller
 *
 */
@AIName("tiamarantaportal")
public class TiamarantaPortalsAI2 extends PortalAI2 {
	
	
	private boolean checkSourceCount(Player player) {
		int count = 0;
		for (final SourceLocation source : SiegeService.getInstance().getSources().values()) {
			if (source.getRace().getRaceId() == player.getRace().getRaceId()) {
				count++;
			}
		}
		if (count >= 2) {
			return true;
		}
		return false;
	}
	
	@Override
	protected void handleUseItemFinish(Player player) {
		if (checkSourceCount(player)) {
			super.handleUseItemFinish(player);
		}
	}
}
