package ai;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.flypath.FlyPathEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Estrayl
 */
@AIName("hidden_teleporter")
public class HiddenTeleportNpcAI extends NpcAI {

	public HiddenTeleportNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1)
			teleport(player);
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

	private void teleport(Player player) {
		int teleId = getTeleportId();
		if (teleId == 0)
			return;
		FlyPathEntry flypath = DataManager.FLY_PATH.getPathTemplate(teleId);
		player.setCurrentFlypath(flypath);
		player.unsetPlayerMode(PlayerMode.RIDE);
		player.setState(CreatureState.FLYING);
		player.unsetState(CreatureState.ACTIVE);
		player.setFlightTeleportId(teleId * 1000 + 1);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, teleId * 1000 + 1, 0), true);
	}

	private int getTeleportId() {
		switch (getOwner().getNpcId()) {
			case 804811:
				return 279;
			case 804812:
				return 281;
			case 804813:
				return 280;
			case 804814:
				return 282;
			case 804822:
				return 286;
			case 804823:
				return 284;
			case 804824:
				return 283;
			case 804825:
				return 285;
		}
		return 0;
	}
}
