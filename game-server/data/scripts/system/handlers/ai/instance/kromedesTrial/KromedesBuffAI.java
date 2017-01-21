package ai.instance.kromedesTrial;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Tiger0319, Gigi, xTz
 */
@AIName("krbuff")
public class KromedesBuffAI extends ActionItemNpcAI {

	@Override
	protected void handleUseItemFinish(Player player) {
		switch (getNpcId()) {
			case 730336:
				SkillEngine.getInstance().applyEffectDirectly(19216, player, player, 0);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400655));
				break;
			case 730337:
				SkillEngine.getInstance().applyEffectDirectly(19217, player, player, 0);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400656));
				AIActions.deleteOwner(this);
				break;
			case 730338:
				SkillEngine.getInstance().applyEffectDirectly(19218, player, player, 0);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400657));
				AIActions.deleteOwner(this);
				break;
			case 730339:
				SkillEngine.getInstance().applyEffectDirectly(19219, player, player, 0);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400658));
				break;
		}
	}
}
