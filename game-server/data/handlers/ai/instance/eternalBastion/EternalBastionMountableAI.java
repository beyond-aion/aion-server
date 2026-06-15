package ai.instance.eternalBastion;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_POSITION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

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
		return true;
	}

	private void useNpc(Player player) {
		if ((getOwner().getTribe() == TribeClass.IDF5_TD_WEAPON_PC || getOwner().getTribe() == TribeClass.IDF5_TD_WEAPON_PC_DARK))
			tryMountNpc(player, 185000136, 21138 + player.getRace().getRaceId());
		else
			tryMountNpc(player, 185000137, 21141);
	}

	private void tryMountNpc(Player player, int keyItemId, int skillId) {
		if (!player.getInventory().decreaseByItemId(keyItemId, 1)) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id()));
			return;
		}
		World.getInstance().updatePosition(player, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
		PacketSendUtility.broadcastPacketAndReceive(player, new SM_POSITION(player));
		SkillEngine.getInstance().applyEffectDirectly(skillId, player, player);
		AIActions.deleteOwner(this);
	}
}
