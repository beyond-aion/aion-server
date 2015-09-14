package ai.siege;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Source
 */
@AIName("siege_gaterepair")
public class GateRepairAI2 extends NpcAI2 {

	@Override
	protected void handleDialogStart(final Player player) {
		RequestResponseHandler gaterepair = new RequestResponseHandler(player) {

			@Override
			public void acceptRequest(Creature requester, Player responder) {
				RequestResponseHandler repairstone = new RequestResponseHandler(player) {

					@Override
					public void acceptRequest(Creature requester, Player responder) {
						onActivate(player);
					}

					@Override
					public void denyRequest(Creature requester, Player responder) {
						// Nothing Happens
					}

				};
				if (player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_DO_YOU_ACCEPT_REPAIR, repairstone))
					PacketSendUtility.sendPacket(player,
						new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_DO_YOU_ACCEPT_REPAIR, player.getObjectId(), 5, new DescriptionId(
							2 * 716568 + 1)));
			}

			@Override
			public void denyRequest(Creature requester, Player responder) {
				// Nothing Happens
			}

		};

		if (player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_POPUPDIALOG, gaterepair))
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_POPUPDIALOG, player.getObjectId(), 5));
	}

	@Override
	protected void handleDialogFinish(Player player) {
	}

	public void onActivate(Player player) {
		// Stert repair process
	}

}
