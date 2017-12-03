package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Source
 */
@AIName("siege_gaterepair")
public class GateRepairAI extends NpcAI {

	public GateRepairAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(final Player player) {
		RequestResponseHandler<Npc> gaterepair = new RequestResponseHandler<Npc>(getOwner()) {

			@Override
			public void acceptRequest(Npc requester, Player responder) {
				RequestResponseHandler<Npc> repairstone = new RequestResponseHandler<Npc>(requester) {

					@Override
					public void acceptRequest(Npc requester, Player responder) {
						onActivate(responder);
					}

				};
				if (player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_DO_YOU_ACCEPT_REPAIR, repairstone))
					PacketSendUtility.sendPacket(player,
						new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_DO_YOU_ACCEPT_REPAIR, getObjectId(), 5, ChatUtil.l10n(716568)));
			}

		};

		if (player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_POPUPDIALOG, gaterepair))
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_POPUPDIALOG, getObjectId(), 5));
	}

	@Override
	protected void handleDialogFinish(Player player) {
	}

	public void onActivate(Player player) {
		// TODO Start repair process
	}

}
