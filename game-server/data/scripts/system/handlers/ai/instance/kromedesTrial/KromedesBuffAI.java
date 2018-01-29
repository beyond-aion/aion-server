package ai.instance.kromedesTrial;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
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

	public KromedesBuffAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		switch (getNpcId()) {
			case 730336:
				SkillEngine.getInstance().applyEffectDirectly(19216, player, player);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDCROMEDE_BUFF_01());
				break;
			case 730337:
				SkillEngine.getInstance().applyEffectDirectly(19217, player, player);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDCROMEDE_BUFF_02());
				AIActions.deleteOwner(this);
				break;
			case 730338:
				SkillEngine.getInstance().applyEffectDirectly(19218, player, player);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDCROMEDE_BUFF_03());
				AIActions.deleteOwner(this);
				break;
			case 730339:
				SkillEngine.getInstance().applyEffectDirectly(19219, player, player);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDCROMEDE_BUFF_04());
				break;
		}
	}
}
