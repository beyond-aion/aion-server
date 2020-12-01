package ai.portals;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.model.templates.teleport.TeleportLocation;
import com.aionemu.gameserver.model.templates.teleport.TeleporterTemplate;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.services.teleport.TeleportService;

import ai.ActionItemNpcAI;

/**
 * @author xTz
 */
@AIName("portal")
public class PortalAI extends ActionItemNpcAI {

	protected TeleporterTemplate teleportTemplate;

	public PortalAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		return true;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		teleportTemplate = DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(getNpcId());
	}

	@Override
	protected void handleDialogStart(Player player) {
		QuestEngine.getInstance().onDialog(new QuestEnv(getOwner(), player, 0, DialogAction.USE_OBJECT));
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		PortalPath portalPath = DataManager.PORTAL2_DATA.getPortalUsePath(getNpcId(), player);
		if (portalPath != null) {
			PortalService.port(portalPath, player, getOwner());
		} else if (teleportTemplate != null) {
			TeleportLocation loc = teleportTemplate.getTeleLocIdData().getTelelocations().get(0);
			if (loc != null) {
				TeleportService.teleport(teleportTemplate, loc.getLocId(), player, getOwner(), TeleportAnimation.FADE_OUT_BEAM);
			}
		} else {
			super.handleUseItemFinish(player);
		}
	}

}
