package ai.instance.rakes;

import java.util.List;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.ActionItemNpcAI;

/**
 * @author xTz
 */
@AIName("centralcannon")
public class CentralDeckMobileCannonAI extends ActionItemNpcAI {

	public CentralDeckMobileCannonAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (!player.getInventory().decreaseByItemId(185000052, 1)) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1111302));
			return;
		}

		if (player.getWorldId() == 300100000) {
			WorldMapInstance worldMapInstance = player.getPosition().getWorldMapInstance();
			// need check
			// getOwner().getController().useSkill(18572);

			killNpc(worldMapInstance.getNpcs(215402));
			killNpc(worldMapInstance.getNpcs(215403));
			killNpc(worldMapInstance.getNpcs(215404));
			killNpc(worldMapInstance.getNpcs(215405));
			killNpc(worldMapInstance.getNpcs(230727));
			killNpc(worldMapInstance.getNpcs(231460));
			killNpc(worldMapInstance.getNpcs(214968));
			spawn(215069, 473.178f, 508.966f, 1032.84f, (byte) 0); // Chief Mate Menekiki
		}
	}

	private void killNpc(List<Npc> npcs) {
		for (Npc npc : npcs) {
			AIActions.kill(this, npc);
		}
	}

}
