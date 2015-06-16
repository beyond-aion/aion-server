package ai.instance.kamarBf;


import ai.portals.PortalAI2;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Luzien
 */
@AIName("kamar_teleport")
public class KamarTeleportAI2 extends PortalAI2 {

	protected int remainingUses;


	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		remainingUses = 25;
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (portalUse != null) {
			PortalPath portalPath = portalUse.getPortalPath(player.getRace());
			if (portalPath != null && checkUsageCount(player)) {
				PortalService.port(portalPath, player, getObjectId());
			}
		}
	}

   private boolean checkUsageCount(Player player) {
	  if (remainingUses <= 0) {
		 PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1500906));
		 return false;
	  }
	  if (--remainingUses == 0)
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1500906));
	  else
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1500905, remainingUses));
	  return true;
   }
}