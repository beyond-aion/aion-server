package ai.worlds;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.templates.LegionDominionInvasionRift;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.npc.SubDialogType;
import com.aionemu.gameserver.model.templates.npc.TalkInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.LegionDominionService;
import com.aionemu.gameserver.services.RiftService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;

@AIName("legion_dominion_invasion_rift_open")
public class LegionDominionInvasionRiftOpenAI extends GeneralNpcAI {

	public LegionDominionInvasionRiftOpenAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		NpcTemplate template = getOwner().getObjectTemplate();
		TalkInfo talkInfo = template.getTalkInfo();
		if (talkInfo == null || talkInfo.getSubDialogType() != SubDialogType.LEGION_DOMINION_NPC)
			return;
		if (LegionDominionService.getInstance().isInCalculationTime())
			return;
		int territoryId = talkInfo.getSubDialogValue();
		LegionDominionLocation loc = LegionDominionService.getInstance().getLegionDominionLoc(territoryId);
		if (loc == null || loc.getInvasionRift() == null)
			return;
		LegionDominionInvasionRift invasionRift = loc.getInvasionRift();
		int dialogPageId = 10;
		if (player.getLegion() == null || player.getLegion().getOccupiedLegionDominion() != territoryId)
			dialogPageId = 1011;
		else if (player.getInventory().getFirstItemByItemId(invasionRift.getKeyItemId()) == null)
			dialogPageId = 27;
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), dialogPageId));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1) {
			int territoryId = getOwner().getObjectTemplate().getTalkInfo().getSubDialogValue();
			LegionDominionLocation loc = LegionDominionService.getInstance().getLegionDominionLoc(territoryId);
			if (loc != null && loc.getInvasionRift() != null) {
				LegionDominionInvasionRift invasionRift = loc.getInvasionRift();
				if (!RiftService.getInstance().isRiftOpened(invasionRift.getRiftId())) {
					if (player.getInventory().decreaseByItemId(invasionRift.getKeyItemId(), 1)) {
						if (!LegionDominionService.getInstance().openInvasionRift(loc.getLocationId())) {
							LoggerFactory.getLogger(LegionDominionInvasionRiftOpenAI.class).error(
								"{} tried to open rift in territory {} - rift opening failed, but item [id={}] got consumed!", player, loc.getLocationId(),
								invasionRift.getKeyItemId());
						}
					}
				}
			}
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

}
