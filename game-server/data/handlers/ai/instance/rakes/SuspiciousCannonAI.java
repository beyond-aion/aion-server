package ai.instance.rakes;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author xTz
 */
@AIName("suspiciouscannon")
public class SuspiciousCannonAI extends ActionItemNpcAI {

	public SuspiciousCannonAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		int teleportId = getOwner().getNpcId() == 730769 ? 247001 : 73001;
		player.setState(CreatureState.FLYING);
		player.unsetState(CreatureState.ACTIVE);
		player.setFlightTeleportId(teleportId);
		PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, teleportId, 0));
	}

}
