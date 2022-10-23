package ai.instance.eternalBastion;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Cheatkiller, Estrayl
 */
@AIName("eternal_bastion_mountable")
public class EternalBastionMountableAI extends ActionItemNpcAI {

	public EternalBastionMountableAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1)
			useNpc(player);
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

	private void useNpc(Player player) {
		if ((getOwner().getTribe() == TribeClass.IDF5_TD_WEAPON_PC || getOwner().getTribe() == TribeClass.IDF5_TD_WEAPON_PC_DARK))
			mountNpc(player, SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5B_TD_DEFWeapon(), 185000136, 21138 + player.getRace().getRaceId());
		else
			mountNpc(player, SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5B_TD_Tank(), 185000137, 21141);
	}

	private void mountNpc(Player player, SM_SYSTEM_MESSAGE denialMsg, int keyItemId, int skillId) {
		if (player.getInventory().decreaseByItemId(keyItemId, 1)) {
			SkillEngine.getInstance().applyEffectDirectly(skillId, player, player);
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			TeleportService.teleportTo(player, getPosition());
			AIActions.deleteOwner(this);
		} else {
			PacketSendUtility.sendPacket(player, denialMsg);
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		}
	}
}
