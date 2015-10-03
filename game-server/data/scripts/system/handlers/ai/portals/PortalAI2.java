package ai.portals;

import ai.ActionItemNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.model.templates.portal.PortalUse;
import com.aionemu.gameserver.model.templates.teleport.TeleportLocation;
import com.aionemu.gameserver.model.templates.teleport.TeleporterTemplate;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author xTz
 */
@AIName("portal")
public class PortalAI2 extends ActionItemNpcAI2 {

	protected TeleporterTemplate teleportTemplate;
	protected PortalUse portalUse;

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		return true;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		teleportTemplate = DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(getNpcId());
		portalUse = DataManager.PORTAL2_DATA.getPortalUse(getNpcId());
	}

	@Override
	protected void handleDialogStart(Player player) {
		AI2Actions.selectDialog(this, player, 0, -1);
		if (getTalkDelay() != 0) {
			super.handleDialogStart(player);
		} else {
			handleUseItemFinish(player);
		}
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (portalUse != null) {
			PortalPath portalPath = portalUse.getPortalPath(player.getRace());
			if (portalPath != null) {
				PortalService.port(portalPath, player, getObjectId());
			}
		} else if (teleportTemplate != null) {
			TeleportLocation loc = teleportTemplate.getTeleLocIdData().getTelelocations().get(0);
			if (loc != null) {
				TeleportService2.teleport(teleportTemplate, loc.getLocId(), player, getOwner(), TeleportAnimation.FADE_OUT_BEAM);
			}
		}
	}

}
